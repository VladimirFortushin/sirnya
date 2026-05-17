package ru.mephi.sirnya.service.notification;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TelegramNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(TelegramNotificationService.class);
    private final String botToken;
    private final String chatId;

    public TelegramNotificationService() {
        try {
            java.util.Properties props = new java.util.Properties();
            props.load(getClass().getClassLoader().getResourceAsStream("telegram.properties"));
            this.botToken = props.getProperty("telegram.bot.token");
            this.chatId = props.getProperty("telegram.chat.id");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Telegram config", e);
        }
    }

    public void sendCode(String destination, String code) {
        String message = String.format("%s, your confirmation code is: %s", destination, code);
        String apiUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        String url = String.format("%s?chat_id=%s&text=%s", apiUrl, chatId, urlEncode(message));
        sendTelegramRequest(url);
    }

    private void sendTelegramRequest(String url) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.error("Telegram API error: {}", response.statusCode());
            } else {
                logger.info("Telegram message sent");
            }
        } catch (InterruptedException e) {
            logger.error("Sending interrupted", e);
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            logger.error("IO error sending Telegram", e);
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}