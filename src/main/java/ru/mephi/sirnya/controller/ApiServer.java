package ru.mephi.sirnya.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.jsonwebtoken.Claims;
import ru.mephi.sirnya.config.DatabaseConnector;
import ru.mephi.sirnya.dao.*;
import ru.mephi.sirnya.model.OtpConfig;
import ru.mephi.sirnya.service.auth.AuthService;
import ru.mephi.sirnya.service.notification.NotificationDispatcher;
import ru.mephi.sirnya.service.otp.OtpService;
import ru.mephi.sirnya.util.JsonUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class ApiServer {
    private final AuthService authService;
    private final OtpService otpService;
    private final UserDao userDao;
    private final OtpConfigDao otpConfigDao;

    public ApiServer() {
        DatabaseConnector connector = new DatabaseConnector();
        UserDao userDao = new UserDao(connector);
        OtpConfigDao otpConfigDao = new OtpConfigDao(connector);
        OtpCodeDao otpCodeDao = new OtpCodeDao(connector);
        NotificationDispatcher dispatcher = new NotificationDispatcher();

        this.authService = new AuthService(userDao);
        this.otpService = new OtpService(otpCodeDao, otpConfigDao, dispatcher);
        this.userDao = userDao;
        this.otpConfigDao = otpConfigDao;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/api/auth/register", exchange -> {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            try {
                Map<String, String> req = JsonUtil.parse(exchange.getRequestBody(), Map.class);
                String login = req.get("login");
                String password = req.get("password");
                String role = req.getOrDefault("role", "USER");
                String token = authService.register(login, password, role);
                sendJsonResponse(exchange, 201, "{\"token\":\"" + token + "\"}");
            } catch (Exception e) {
                sendError(exchange, 400, e.getMessage());
            }
        });

        server.createContext("/api/auth/login", exchange -> {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            try {
                Map<String, String> req = JsonUtil.parse(exchange.getRequestBody(), Map.class);
                String token = authService.login(req.get("login"), req.get("password"));
                sendJsonResponse(exchange, 200, "{\"token\":\"" + token + "\"}");
            } catch (Exception e) {
                sendError(exchange, 401, e.getMessage());
            }
        });

        server.createContext("/api/user/otp/generate", exchange -> {
            Claims claims = authenticate(exchange);
            if (claims == null) return;
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            int userId = claims.get("userId", Integer.class);
            try {
                Map<String, String> req = JsonUtil.parse(exchange.getRequestBody(), Map.class);
                String operationId = req.get("operationId");
                String channel = req.get("channel");
                String destination = req.get("destination");
                otpService.generateAndSend(userId, operationId, channel, destination);
                sendJsonResponse(exchange, 200, "{\"message\":\"Code sent\"}");
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage());
            }
        });

        server.createContext("/api/user/otp/validate", exchange -> {
            Claims claims = authenticate(exchange);
            if (claims == null) return;
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            int userId = claims.get("userId", Integer.class);
            try {
                Map<String, String> req = JsonUtil.parse(exchange.getRequestBody(), Map.class);
                String operationId = req.get("operationId");
                String code = req.get("code");
                boolean valid = otpService.validateCode(userId, operationId, code);
                sendJsonResponse(exchange, 200, "{\"valid\":" + valid + "}");
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage());
            }
        });
        server.createContext("/api/admin/otp-config", exchange -> {
            Claims claims = authenticateAdmin(exchange);
            if (claims == null) return;
            if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    OtpConfig cfg = otpConfigDao.getConfig();
                    String json = JsonUtil.toJson(cfg);
                    sendJsonResponse(exchange, 200, json);
                } catch (Exception e) {
                    sendError(exchange, 500, e.getMessage());
                }
            } else if ("PUT".equals(exchange.getRequestMethod())) {
                try {
                    OtpConfig newCfg = JsonUtil.parse(exchange.getRequestBody(), OtpConfig.class);
                    otpConfigDao.updateConfig(newCfg);
                    sendJsonResponse(exchange, 200, "{\"status\":\"updated\"}");
                } catch (Exception e) {
                    sendError(exchange, 400, e.getMessage());
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });

        server.createContext("/api/admin/users", exchange -> {
            Claims claims = authenticateAdmin(exchange);
            if (claims == null) return;
            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            try {
                List<?> users = userDao.findAllExceptAdmins();
                String json = JsonUtil.toJson(users);
                sendJsonResponse(exchange, 200, json);
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage());
            }
        });

        server.createContext("/api/admin/users/delete", exchange -> {
            Claims claims = authenticateAdmin(exchange);
            if (claims == null) return;
            if (!"DELETE".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            try {
                Map<String, Object> req = JsonUtil.parse(exchange.getRequestBody(), Map.class);
                int userId = (int) req.get("userId");
                userDao.deleteOtpCodesByUser(userId);
                userDao.deleteUser(userId);
                sendJsonResponse(exchange, 200, "{\"status\":\"deleted\"}");
            } catch (Exception e) {
                sendError(exchange, 400, e.getMessage());
            }
        });

        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        System.out.println("Server started on port 8080");
    }

    private Claims authenticate(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(exchange, 401, "Missing or invalid Authorization header");
            return null;
        }
        String token = authHeader.substring(7);
        try {
            return authService.validateToken(token);
        } catch (Exception e) {
            sendError(exchange, 401, "Invalid or expired token");
            return null;
        }
    }

    private Claims authenticateAdmin(HttpExchange exchange) throws IOException {
        Claims claims = authenticate(exchange);
        if (claims == null) return null;
        String role = claims.get("role", String.class);
        if (!"ADMIN".equals(role)) {
            sendError(exchange, 403, "Admin access required");
            return null;
        }
        return claims;
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        String json = "{\"error\":\"" + message + "\"}";
        sendJsonResponse(exchange, statusCode, json);
    }
}
