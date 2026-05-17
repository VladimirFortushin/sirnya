package ru.mephi.sirnya.controller.filter;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import io.jsonwebtoken.Claims;
import ru.mephi.sirnya.service.auth.AuthService;

import java.io.IOException;

public class AuthFilter extends Filter {
    private final AuthService authService;

    public AuthFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }
        String token = authHeader.substring(7);
        try {
            Claims claims = authService.validateToken(token);
            exchange.setAttribute("claims", claims);
            chain.doFilter(exchange);
        } catch (Exception e) {
            exchange.sendResponseHeaders(401, -1);
        }
    }

    @Override
    public String description() {
        return "JWT Authentication Filter";
    }
}