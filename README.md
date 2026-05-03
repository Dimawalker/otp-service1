# OTP Service

Сервис для генерации и отправки одноразовых кодов подтверждения (OTP) через Email, SMS, Telegram и файл.

## Технологии
- Java 17
- Gradle
- PostgreSQL 17
- JWT аутентификация
- Jakarta Mail (SMTP)
- jSMPP (SMS эмулятор)
- Telegram Bot API

## Запуск проекта

### Требования
- Java 17
- PostgreSQL 17
- Gradle

### Настройка базы данных
```sql
CREATE DATABASE otp_service;
-- Выполнить init.sql

Настройка конфигурации
src/main/resources/application.properties — подключение к БД

src/main/resources/email.properties — SMTP настройки

src/main/resources/sms.properties — SMPP настройки

src/main/resources/telegram.properties — Telegram токен

Запуск
bash
./gradlew run
API Эндпоинты
Публичные
Метод	Эндпоинт	Описание
POST	/api/auth/register	Регистрация пользователя
POST	/api/auth/login	Логин, возвращает JWT токен
Защищённые (требуют Bearer токен)
Метод	Эндпоинт	Описание
POST	/api/otp/generate	Генерация OTP кода
POST	/api/otp/validate	Валидация OTP кода
GET	/api/test	Тестовый эндпоинт
Административные (только ADMIN)
Метод	Эндпоинт	Описание
GET	/api/admin/users	Список пользователей
DELETE	/api/admin/users/{id}	Удаление пользователя
PUT	/api/admin/config	Изменение конфигурации OTP
Каналы доставки
EMAIL — реальная отправка через SMTP (Mail.ru)

SMS — отправка через SMPP эмулятор

TELEGRAM — отправка через Telegram Bot API

FILE — сохранение в файл otp_codes.log
