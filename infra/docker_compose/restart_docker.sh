#!/bin/bash

# Путь до файла конфигурации браузеров и docker-compose относительно папки скрипта
JSON_FILE="../../config/browsers.json"
COMPOSE_FILE="./docker-compose.yml"

echo ">>> Остановить старую инфраструктуру Docker Compose"
docker compose -f "$COMPOSE_FILE" down

echo ">>> Docker pull все образы браузеров из конфигурации"

# Проверяем, что jq установлен
if ! command -v jq &> /dev/null; then
    echo "❌ jq не установлен. Пожалуйста, установите jq (например, через 'winget install jqlang.jq' или 'brew install jq') и повторите попытку."
    exit 1
fi

# Проверяем, что файл браузеров существует
if [ ! -f "$JSON_FILE" ]; then
    echo "❌ Файл конфигурации не найден по пути: $JSON_FILE"
    exit 1
fi

# Извлекаем все значения .image через jq и принудительно удаляем скрытые символы \r
images=$(jq -r '.. | objects | select(.image) | .image' "$JSON_FILE" | tr -d '\r')

# Пробегаем по каждому образу и выполняем docker pull
for image in $images; do
    # Убираем лишние пробелы
    clean_image=$(echo "$image" | tr -d ' ')
    echo "📥 Скачивание: $clean_image..."
    docker pull "$clean_image"
done


echo ">>> Запуск инфраструктуры TeamCity + Selenoid"
docker compose -f "$COMPOSE_FILE" up -d

echo "✅ Инфраструктура успешно поднята в фоновом режиме!"
