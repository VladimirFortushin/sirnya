package ru.mephi.sirnya.service.otp;

import ru.mephi.sirnya.dao.OtpCodeDao;
import ru.mephi.sirnya.dao.OtpConfigDao;
import ru.mephi.sirnya.model.OtpCode;
import ru.mephi.sirnya.model.OtpConfig;
import ru.mephi.sirnya.service.notification.NotificationDispatcher;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OtpService {
    private final OtpCodeDao otpCodeDao;
    private final OtpConfigDao otpConfigDao;
    private final NotificationDispatcher dispatcher;
    public OtpService(OtpCodeDao otpCodeDao, OtpConfigDao otpConfigDao, NotificationDispatcher dispatcher) {
        this.otpCodeDao = otpCodeDao;
        this.otpConfigDao = otpConfigDao;
        this.dispatcher = dispatcher;
    }

    public String generateAndSend(int userId, String operationId, String channel, String destination) throws SQLException {
        OtpConfig config = otpConfigDao.getConfig();
        String code = generateNumericCode(config.getCodeLength());
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(config.getLifetimeSeconds());

        OtpCode otpCode = new OtpCode();
        otpCode.setUserId(userId);
        otpCode.setOperationId(operationId);
        otpCode.setCode(code);
        otpCode.setStatus("ACTIVE");
        otpCode.setExpiresAt(expiresAt);
        otpCodeDao.save(otpCode);

        dispatcher.send(channel, destination, code);
        return code;
    }

    public boolean validateCode(int userId, String operationId, String code) throws SQLException {
        Optional<OtpCode> opt = otpCodeDao.findActiveByUserAndOperation(userId, operationId);
        if (opt.isEmpty()) return false;
        OtpCode otpCode = opt.get();
        if (otpCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpCodeDao.updateStatus(otpCode.getId(), "EXPIRED");
            return false;
        }
        if (!otpCode.getCode().equals(code)) return false;
        otpCodeDao.updateStatus(otpCode.getId(), "USED");
        return true;
    }

    private String generateNumericCode(int length) {
        SecureRandom random = new SecureRandom();
        return IntStream.range(0, length)
                .map(i -> random.nextInt(10))
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());
    }
}
