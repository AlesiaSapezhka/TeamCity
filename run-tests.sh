#!/bin/bash

# Настройка
IMAGE_NAME=teamcity-tests
TEST_PROFILE=$1 # Аргумент запуска (api/ui). Если пустой — включится параллельный веер.
TIMESTAMP=$(date +"%Y%m%d_%H%M")
TEST_OUTPUT_DIR=$PWD/test-output/$TIMESTAMP
ALLURE_RESULTS_DIR=$PWD/allure-results
ALLURE_REPORT_DIR=$TEST_OUTPUT_DIR/allure-report

# 1. Автоматически извлекаем супертокен из контейнера на хосте
echo ">>> Извлечение супертокена из логов TeamCity..."
RAW_TOKEN=$(docker logs teamcity-server 2>&1 | grep -i 'Super user authentication token' | tail -1)
FETCHED_TOKEN=$(echo "$RAW_TOKEN" | grep -oE '[0-9]+' | tr -d '\r\n ')

if [ -z "$FETCHED_TOKEN" ]; then
  echo "Не удалось автоматически найти токен в логах контейнера teamcity-server."
  echo "Будет использовано дефолтное значение 'auto'."
  FETCHED_TOKEN="auto"
else
  echo "Супертокен успешно извлечен и передан в переменные окружения."
fi

# Собираем Docker образ
echo ">>> Сборка тестов запущена"
docker build -t $IMAGE_NAME .

# Создаем структуру папок
mkdir -p "$TEST_OUTPUT_DIR/logs"
mkdir -p "$ALLURE_RESULTS_DIR"

# Функция для запуска отдельного Docker-контейнера в фоне
run_container_flow() {
  local profile=$1
  local browser=$2
  echo "🚀 Запуск потока: Профиль [$profile], Браузер [$browser]..."

  MSYS_NO_PATHCONV=1 docker run --rm \
    --add-host=host.docker.internal:host-gateway \
    -v "$TEST_OUTPUT_DIR/logs":/app/logs \
    -v "$ALLURE_RESULTS_DIR/${profile}_${browser}":/app/allure-results \
    -e TEST_PROFILE="$profile" \
    -e APIBASEURL=http://host.docker.internal:8111 \
    -e UIBASEURL=http://host.docker.internal:8111 \
    -e SUPERUSER_TOKEN="$FETCHED_TOKEN" \
    $IMAGE_NAME mvn test -P "$profile" -Dbrowser="$browser" > "$TEST_OUTPUT_DIR/logs/${profile}_${browser}.log" 2>&1 &
}

# 2. ПРОВЕРКА: Запускать параллельно или один поток?
if [ -n "$TEST_PROFILE" ]; then
  # ЕСЛИ АРГУМЕНТ ЕСТЬ: Запускаем один контейнер (как раньше)
  echo ">>> Запущен одиночный поток для профиля: $TEST_PROFILE"
  run_container_flow "$TEST_PROFILE" "chrome"
  wait
else
  # ЕСЛИ АРГУМЕНТА НЕТ (Клик по стрелочке): Запускаем «веер» параллельно в фоне
  echo ">>> Запуск параллельного тестирования (API + UI Chrome/Firefox/Opera)..."

  # Копируем историю Allure прошлых запусков (для графиков трендов)
  LAST_REPORT=$(ls -td $PWD/test-output/*/allure-report 2>/dev/null | head -1)
  if [ -d "$LAST_REPORT/history" ]; then
      echo ">>> Подтягивание Allure истории из предыдущего прогона..."
      cp -r "$LAST_REPORT/history" "$ALLURE_RESULTS_DIR/history"
  fi

  # Запускаем 4 фоновых процесса одновременно (благодаря знаку & внутри функции)
  run_container_flow "api" "chrome"
  run_container_flow "ui" "chrome"
  run_container_flow "ui" "firefox"
  run_container_flow "ui" "opera"

  echo "⏳ Ожидание завершения выполнения всех параллельных потоков..."
  wait # Ждем, пока все 4 контейнера финишируют
fi

# 3. Проверка Checkstyle (валидация кода)
echo ">>> Проверка качества кода (Checkstyle)..."
MSYS_NO_PATHCONV=1 docker run --rm $IMAGE_NAME mvn checkstyle:check > "$TEST_OUTPUT_DIR/logs/checkstyle.log" 2>&1

# 4. Схлопывание в один Allure отчет
if [ -d "$ALLURE_RESULTS_DIR" ]; then
    echo ">>> Генерация единого Allure отчета..."
    if command -v allure &> /dev/null; then
        allure generate "$ALLURE_RESULTS_DIR" -o "$ALLURE_REPORT_DIR" --clean
        echo "📊 Allure отчет успешно сгенерирован: $ALLURE_REPORT_DIR/index.html"
    else
        echo "⚠️ Утилита allure-cli не найдена на хосте. Сырые результаты сохранены в: $ALLURE_RESULTS_DIR"
    fi
fi

echo ">>> Все параллельные тесты завершены!"
