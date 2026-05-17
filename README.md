# OTP Verification Service 
Серверное приложение для генерации, рассылки и валидации одноразовых кодов подтверждения (OTP). 
Реализовано на Java 26 с использованием встроенного HTTP-сервера `com.sun.net.httpserver`, PostgreSQL 17, JDBC, JWT, BCrypt, а также каналов отправки: Email, SMS (SMPP‑эмулятор), Telegram и сохранение в файл. 
Проект выполнен в рамках учебного задания «Промо IT» и может быть использован как основа для портфолио Java‑разработчика. 

--- ## Возможности - Регистрация и аутентификация пользователей (роли `ADMIN`, `USER`). 
- Генерация OTP‑кода с настраиваемой длиной и временем жизни.
- Отправка кода по каналам: - Email (SMTP) - SMS (через SMPP‑эмулятор, например SMPPSim) - Telegram (через Telegram Bot API) - Файл (сохранение в корне проекта)
- Валидация полученного кода (статусы: ACTIVE, EXPIRED, USED).
- Административное API: - Изменение конфигурации OTP (длина и время жизни).
- Просмотр списка обычных пользователей.
- Удаление пользователя вместе с его кодами.
- Автоматическая пометка просроченных кодов (фоновый планировщик).
- Логирование (Logback).

--- ## Методы 
- POST /api/auth/register - Регистрация нового пользователя 
- POST /api/auth/login - Аутентификация, получение JWT
- POST /api/user/otp/generate - Генерация и отправка OTP‑кода
- POST /api/user/otp/validate - Проверка OTP‑кода 
- GET /api/admin/otp-config - Получить текущую конфигурацию OTP 
- ADMIN PUT /api/admin/otp-config - Изменить конфигурацию OTP ADMIN 
- GET /api/admin/users - Список всех пользователей, кроме админов ADMIN 
- DELETE /api/admin/users/delete - Удалить пользователя и его OTP‑коды ADMIN 

--- ## Требования 
- **Java 26**
- **Maven 3.8+**
- **PostgreSQL 17** (или совместимая версия)
- Для каналов рассылки: - Email: рабочий SMTP‑сервер или эмулятор - SMS: эмулятор SMPP (например [SMPPSim](http://www.seleniumsoftware.com/downloads.html)) - Telegram: бот, созданный через [@BotFather](https://t.me/BotFather)

--- ## Запуск (терминал) 
cd в директорию нахождения sirnya.jar, 
применить схему .sql: psql -d sirnya -f schema.sql 
запуск .jar: java -jar sirnya.jar 

--- ## Примеры запросов 
Регистрация администратора или юзера (role:"USER")
```
curl -X POST http://localhost:8080/api/auth/register \
-H "Content-Type: application/json" \
-d '{"login":"admin","password":"admin","role":"ADMIN"}'
```
```Ответ
{"token":"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiIsInVzZXJJZCI6MSwiaWF0IjoxNzc5MDA5MTY2LCJleHAiOjE3NzkwMTI3NjZ9.Dp5apm_Kok0csa7-eO65db7GrKDL3Ek5MOLGfw7i-qc"} 
```
Занесение токена в переменную 
```
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"login":"admin","password":"admin"}' | sed 's/.*"token":"\(.*\)"}/\1/')
```
Логин 
```
curl -X POST http://localhost:8080/api/auth/login \
-H "Content-Type: application/json" \
-d '{"login":"admin","password":"admin"}'
```
Генерация OTP (канал FILE)
```
curl -X POST http://localhost:8080/api/user/otp/generate \
-H "Authorization: Bearer $USER_TOKEN" \
-H "Content-Type: application/json" \
-d '{"operationId":"op1","channel":"FILE","destination":"test"}'
```
```Ответ 
{"message":"Code sent"}
```
После этого в корневой папке проекта появится файл otp_code_test.txt с кодом. 
```
OTP code: 166733
```
Валидация кода 
```
curl -X POST http://localhost:8080/api/user/otp/validate
-H "Authorization: Bearer $USER_TOKEN" -H "Content-Type: application/json"
-d '{"operationId":"op1","code":"166733"}'
```
```Ответ 
{"valid":true}
```
Изменение конфигурации OTP (администратор) (предварительно зарегистрировать админа и применить его $ADMIN_TOKEN)
```
curl -X PUT http://localhost:8080/api/admin/otp-config \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"codeLength":8,"lifetimeSeconds":600}'
```
Просмотр пользователей (админ)
```
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```
Удаление пользователя (админ)
```
curl -X DELETE http://localhost:8080/api/admin/users/delete \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"userId":2}'
```
