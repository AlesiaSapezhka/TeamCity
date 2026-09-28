name: Checkstyle & Parallel Automation Tests

on:
  push:
    branches: [ nikita_ci ]
  workflow_dispatch:

jobs:
  run-test-automation:
    name: Code Control & Run Parallel Tests
    runs-on: ubuntu-latest

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: 'corretto'
          java-version: '21'
          cache: 'maven'

      # 1. Скачивание браузеров и старт инфраструктуры
      - name: Start Test Infrastructure
        run: |
          cd infra/docker_compose
          chmod +x restart_docker.sh
          ./restart_docker.sh

      # Выжидаем прогрев TeamCity
      - name: Wait for Server Warmup
        run: sleep 40

      # 2. УСТАНОВКА ALLURE CLI
      - name: Install Allure CLI
        run: |
          sudo apt-get update
          sudo apt-get install -y npm
          sudo npm install -g allure-commandline --save-dev
          allure --version

      # 3. Запуск веерного скрипта (Checkstyle + API + 3 Browsers + Allure)
      - name: Run Parallel Tests and Generate Report
        run: |
          chmod +x run-tests.sh
          ./run-tests.sh

      # 4. Сохранение чистого HTML Allure отчета в артефакты
      - name: Upload Allure Report Artifact
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: allure-report-artifact
          path: test-output/*/allure-report/
          retention-days: 7
