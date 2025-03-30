# Витрина интернет-магазина

Веб-приложение «Витрина интернет-магазина», написанное с использованием с использованием Spring Boot на реактивном стеке технологий.
! Внимание. Для запуска приложения необходимо наличие установленного Docker.
## Использование

Для запуска тестов использовать команду:

```sh
./gradlew :payment-service:unit
./gradlew :internet-shop:unit
```

Для корректной работы указать переменные среды(переменные окружения):
```
- POSTGRES_USER
- POSTGRES_PASSWORD
```

Для запуска интеграционных тестов для витрины нужно запустить сервис платежей:
```sh
docker pull postgres:17-alpine && docker run -d --name pg-payments -p 5433:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=payments postgres:17-alpine && ./gradlew :payment-service:bootRun
```

Для запуска в докере вместе с PostgreSQL и запуска его на порту 8080 выполнить:

```sh
docker compose up
```
Для остановки сервисов:

```sh
docker compose stop
```
После запуска приложение будет доступно по адресу:

```
http://<адрес контейнера>/shop/product
```