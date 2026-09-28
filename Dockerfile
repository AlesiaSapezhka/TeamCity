# базовый докер образ
# каждый раз с нуля строить базовый образ (java, mvn, git)
# голый докер образ и устанавлить java, maven
# ЕСЛИ МОЖНО СОЗДАТЬ ОБРАЗ ПОВЕРХ ДРУГОГО ОБРАЗА, ГДЕ ВСЕ УЖЕ УСТАНОВЛЕНО
# МАРКЕТПЛЕЙС ВСЕХ ДОКЕР ОБРАЗОВ - docker hub
FROM maven:3.9.11-eclipse-temurin-21

# Дефолтные значения аргументов
ARG TEST_PROFILE=api
ARG APIBASEURL=http://localhost:8111
ARG UIBASEURL=http://host.docker.internal:8111

# Переменные окружения для контейнера
ENV TEST_PROFILE=${TEST_PROFILE}
ENV APIBASEURL=${APIBASEURL}
ENV UIBASEURL=${UIBASEURL}

# работаем из папки /app
WORKDIR /app

# копируем помник
COPY pom.xml .

# загружаем зависимости и кешируем
RUN mvn dependency:go-offline

# копируем весь проект
COPY . .

# теперь внутри есть зависимости, есть весь проект и мы готовы запускать тесты

USER root

# mvn test -P api
# mvn -DskipTests=true surfire-report:report
# лог выводился не в консоль, а в файл
# bash file
CMD ["mvn", "clean", "test"]

