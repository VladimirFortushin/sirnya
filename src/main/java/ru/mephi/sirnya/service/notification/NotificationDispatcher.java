package ru.mephi.sirnya.service.notification;

public class NotificationDispatcher {
    private final EmailNotificationService emailService;
    private final SmsNotificationService smsService;
    private final TelegramNotificationService telegramService;
    private final FileCodeSaver fileSaver;

    public NotificationDispatcher() {
        this.emailService = new EmailNotificationService();
        this.smsService = new SmsNotificationService();
        this.telegramService = new TelegramNotificationService();
        this.fileSaver = new FileCodeSaver();
    }

    public void send(String channel, String destination, String code) {
        switch (channel.toUpperCase()) {
            case "EMAIL": emailService.sendCode(destination, code); break;
            case "SMS":   smsService.sendCode(destination, code); break;
            case "TELEGRAM": telegramService.sendCode(destination, code); break;
            case "FILE":  fileSaver.saveCode(destination, code); break;
            default: throw new IllegalArgumentException("Unknown channel: " + channel);
        }
    }
}