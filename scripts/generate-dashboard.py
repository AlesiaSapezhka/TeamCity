#!/usr/bin/env python3
"""Build manager Automation Dashboard from Allure results, git, and Swagger coverage."""

from __future__ import annotations

import argparse
import json
import re
import subprocess
from collections import defaultdict
from datetime import datetime, timezone
from pathlib import Path


TEST_ANNOTATION = re.compile(
    r"^\s*@(?:Test|ParameterizedTest|RepeatedTest|TestFactory)\b",
    re.MULTILINE,
)
ADDED_TEST_ANNOTATION = re.compile(
    r"^\+\s*@(?:Test|ParameterizedTest|RepeatedTest|TestFactory)\b",
    re.MULTILINE,
)
REMOVED_TEST_ANNOTATION = re.compile(
    r"^\-\s*@(?:Test|ParameterizedTest|RepeatedTest|TestFactory)\b",
    re.MULTILINE,
)
SWAGGER_ALL = re.compile(r"All operations:\s*(\d+)", re.IGNORECASE)
SWAGGER_WITHOUT = re.compile(r"Operations without calls:\s*(\d+)", re.IGNORECASE)


def read_json(path: Path):
    with path.open(encoding="utf-8") as f:
        return json.load(f)


def write_json(path: Path, data) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
        f.write("\n")


def suite_of(result: dict, fallback_dir: str | None = None) -> str:
    for label in result.get("labels") or []:
        if label.get("name") == "parentSuite" and label.get("value"):
            return label["value"]
    for param in result.get("parameters") or []:
        if param.get("name") == "Suite" and param.get("value"):
            return param["value"]
    return fallback_dir or "unknown"


def unique_key(result: dict) -> str:
    history_id = result.get("historyId") or ""
    if "::" in history_id:
        history_id = history_id.rsplit("::", 1)[0]
    full_name = result.get("fullName") or result.get("name") or result.get("uuid") or ""
    return history_id or full_name


def result_duration_ms(result: dict) -> float | None:
    start = result.get("start")
    stop = result.get("stop")
    if isinstance(start, (int, float)) and isinstance(stop, (int, float)) and stop >= start:
        return float(stop - start)
    return None


def summarize_executions(results: list[dict]) -> tuple[dict, dict]:
    statuses = defaultdict(int)
    suites: dict[str, dict] = defaultdict(
        lambda: {
            "passed": 0,
            "failed": 0,
            "broken": 0,
            "skipped": 0,
            "total": 0,
            "duration_sec": 0.0,
        }
    )
    durations_ms: list[float] = []
    unique = set()

    for result in results:
        status = (result.get("status") or "unknown").lower()
        if status not in ("passed", "failed", "broken", "skipped"):
            status = "failed"
        statuses[status] += 1

        suite = suite_of(result)
        suites[suite]["total"] += 1
        if status in suites[suite]:
            suites[suite][status] += 1

        unique.add(unique_key(result))

        duration = result_duration_ms(result)
        if duration is not None:
            durations_ms.append(duration)
            suites[suite]["duration_sec"] += duration / 1000.0

    for suite_data in suites.values():
        suite_data["duration_sec"] = round(suite_data["duration_sec"], 1)

    passed = statuses.get("passed", 0)
    failed = statuses.get("failed", 0)
    broken = statuses.get("broken", 0)
    skipped = statuses.get("skipped", 0)
    total = sum(statuses.values())
    completed = passed + failed + broken
    pass_rate = (passed / completed * 100.0) if completed else 0.0
    avg_sec = (sum(durations_ms) / len(durations_ms) / 1000.0) if durations_ms else None

    api_sec = suites.get("api", {}).get("duration_sec") or 0.0
    ui_chrome_sec = suites.get("ui-chrome", {}).get("duration_sec") or 0.0
    ui_firefox_sec = suites.get("ui-firefox", {}).get("duration_sec") or 0.0
    # Matrix jobs run in parallel — wall time for UI ≈ slower browser suite
    ui_wall_sec = max(ui_chrome_sec, ui_firefox_sec) if (ui_chrome_sec or ui_firefox_sec) else 0.0

    executions = {
        "total": total,
        "passed": passed,
        "failed": failed,
        "broken": broken,
        "skipped": skipped,
        "pass_rate": round(pass_rate, 2),
        "unique_scenarios": len(unique),
        "avg_duration_sec": round(avg_sec, 2) if avg_sec is not None else None,
        "api_duration_sec": round(api_sec, 1) if api_sec else None,
        "ui_chrome_duration_sec": round(ui_chrome_sec, 1) if ui_chrome_sec else None,
        "ui_firefox_duration_sec": round(ui_firefox_sec, 1) if ui_firefox_sec else None,
        "ui_duration_sec": round(ui_wall_sec, 1) if ui_wall_sec else None,
        "total_duration_sec": round(api_sec + ui_wall_sec, 1) if (api_sec or ui_wall_sec) else None,
    }
    return executions, dict(suites)


def count_tests_in_file(path: Path) -> int:
    try:
        text = path.read_text(encoding="utf-8")
    except OSError:
        return 0
    return len(TEST_ANNOTATION.findall(text))


def net_new_tests_since(repo_root: Path, days: int) -> int:
    """Count net @Test additions in diffs (works for new methods in existing files)."""
    try:
        out_bytes = subprocess.check_output(
            [
                "git",
                "-C",
                str(repo_root),
                "log",
                f"--since={days}.days.ago",
                "-p",
                "--unified=0",
                "--",
                "src/test/java",
            ],
            stderr=subprocess.DEVNULL,
        )
        out = out_bytes.decode("utf-8", errors="replace")
    except (subprocess.CalledProcessError, FileNotFoundError):
        return 0
    added = len(ADDED_TEST_ANNOTATION.findall(out))
    removed = len(REMOVED_TEST_ANNOTATION.findall(out))
    return max(added - removed, 0)


def git_test_metrics(repo_root: Path) -> dict:
    test_root = repo_root / "src" / "test" / "java"
    total = 0
    api_tests = 0
    ui_tests = 0
    if test_root.is_dir():
        for path in test_root.rglob("*Test.java"):
            count = count_tests_in_file(path)
            total += count
            rel = path.relative_to(test_root).as_posix()
            if rel.startswith("api/"):
                api_tests += count
            elif rel.startswith("ui/"):
                ui_tests += count

    return {
        "total_tests": total,
        "api_tests": api_tests,
        "ui_tests": ui_tests,
        "new_tests_7d": net_new_tests_since(repo_root, 7),
        "new_tests_30d": net_new_tests_since(repo_root, 30),
    }


def parse_swagger_html(path: Path | None) -> dict:
    empty = {
        "all_operations": None,
        "operations_without_calls": None,
        "covered_operations": None,
        "coverage_percent": None,
    }
    if path is None or not path.is_file():
        return empty
    text = path.read_text(encoding="utf-8", errors="ignore")
    all_m = SWAGGER_ALL.search(text)
    without_m = SWAGGER_WITHOUT.search(text)
    if not all_m or not without_m:
        return empty
    all_ops = int(all_m.group(1))
    without = int(without_m.group(1))
    covered = max(all_ops - without, 0)
    pct = (covered / all_ops * 100.0) if all_ops else 0.0
    return {
        "all_operations": all_ops,
        "operations_without_calls": without,
        "covered_operations": covered,
        "coverage_percent": round(pct, 2),
    }


def load_history(path: Path) -> list[dict]:
    if not path.is_file():
        return []
    try:
        data = read_json(path)
    except (OSError, json.JSONDecodeError):
        return []
    if isinstance(data, list):
        return data
    if isinstance(data, dict) and isinstance(data.get("history"), list):
        return data["history"]
    return []


def append_history(history: list[dict], point: dict, limit: int = 40) -> list[dict]:
    history = [h for h in history if h.get("run_number") != point.get("run_number")]
    history.append(point)
    history.sort(key=lambda h: h.get("run_number") or 0)
    return history[-limit:]


def render_html(template_path: Path, metrics: dict) -> str:
    template = template_path.read_text(encoding="utf-8")
    payload = json.dumps(metrics, ensure_ascii=False)
    return template.replace("__METRICS_JSON__", payload)


def load_results(results_dir: Path) -> list[dict]:
    by_dir: list[dict] = []
    if not results_dir.is_dir():
        return by_dir
    for suite_dir in sorted(p for p in results_dir.iterdir() if p.is_dir()):
        if suite_dir.name in ("_meta", "history"):
            continue
        for path in suite_dir.glob("*-result.json"):
            try:
                item = read_json(path)
            except (OSError, json.JSONDecodeError):
                continue
            if not any(l.get("name") == "parentSuite" for l in (item.get("labels") or [])):
                item.setdefault("labels", []).append(
                    {"name": "parentSuite", "value": suite_dir.name}
                )
            by_dir.append(item)
    return by_dir


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--results-dir", type=Path, required=True)
    parser.add_argument("--site-dir", type=Path, required=True)
    parser.add_argument("--template", type=Path, required=True)
    parser.add_argument("--history-in", type=Path, default=None)
    parser.add_argument("--swagger-report", type=Path, default=None)
    parser.add_argument("--repo-root", type=Path, default=Path("."))
    parser.add_argument("--run-number", default="")
    parser.add_argument("--run-id", default="")
    parser.add_argument("--branch", default="")
    parser.add_argument("--commit", default="")
    parser.add_argument("--build-url", default="")
    args = parser.parse_args()

    results = load_results(args.results_dir)
    executions, suites = summarize_executions(results)
    git = git_test_metrics(args.repo_root)
    swagger = parse_swagger_html(args.swagger_report)

    now = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
    date_only = datetime.now(timezone.utc).strftime("%Y-%m-%d")

    history = load_history(args.history_in) if args.history_in else []

    try:
        run_number_int = int(args.run_number) if args.run_number else None
    except ValueError:
        run_number_int = None

    point = {
        "run_number": run_number_int or args.run_number,
        "date": date_only,
        "pass_rate": executions["pass_rate"],
        "executions_total": executions["total"],
        "unique_scenarios": executions["unique_scenarios"],
        "git_total_tests": git["total_tests"],
        "swagger_coverage_percent": swagger["coverage_percent"],
        "api_duration_sec": executions.get("api_duration_sec"),
        "ui_duration_sec": executions.get("ui_duration_sec"),
    }
    history = append_history(history, point)

    metrics = {
        "generated_at": now,
        "run": {
            "number": args.run_number,
            "id": args.run_id,
            "branch": args.branch,
            "commit": args.commit,
            "date": date_only,
            "build_url": args.build_url,
        },
        "executions": executions,
        "suites": suites,
        "git": git,
        "swagger": swagger,
        "history": history,
    }

    site = args.site_dir
    site.mkdir(parents=True, exist_ok=True)
    (site / "data").mkdir(parents=True, exist_ok=True)

    write_json(site / "data" / "metrics.json", metrics)
    write_json(site / "data" / "metrics-history.json", history)

    html = render_html(args.template, metrics)
    (site / "index.html").write_text(html, encoding="utf-8")

    print(
        f"Dashboard written to {site / 'index.html'} "
        f"(executions={executions['total']}, pass_rate={executions['pass_rate']}%, "
        f"git_tests={git['total_tests']}, new_7d={git['new_tests_7d']}, "
        f"api={executions.get('api_duration_sec')}s, ui={executions.get('ui_duration_sec')}s)"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
