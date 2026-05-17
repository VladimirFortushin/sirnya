package ru.mephi.sirnya.service.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.mindrot.jbcrypt.BCrypt;
import ru.mephi.sirnya.dao.UserDao;
import ru.mephi.sirnya.model.User;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class AuthService {
    private final UserDao userDao;
    private final String jwtSecret = "your-secret-key-change-in-production";
    private final long jwtExpirationMs = 3600000; // 1 час

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public String register(String login, String password, String role) throws Exception {
        if (userDao.findByLogin(login).isPresent()) {
            throw new RuntimeException("User already exists");
        }
        if ("ADMIN".equalsIgnoreCase(role)) {
            if (userDao.countByRole("ADMIN") > 0) {
                throw new RuntimeException("Admin already exists");
            }
        }
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User();
        user.setLogin(login);
        user.setPasswordHash(hash);
        user.setRole(role.toUpperCase());
        userDao.save(user);
        return generateToken(user);
    }

    public String login(String login, String password) throws Exception {
        User user = userDao.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        return generateToken(user);
    }

    private String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getLogin())
                .claim("role", user.getRole())
                .claim("userId", user.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
