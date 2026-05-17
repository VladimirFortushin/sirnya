package ru.mephi.sirnya.scheduler;

import ru.mephi.sirnya.dao.OtpCodeDao;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OtpExpirationScheduler {
    private final OtpCodeDao otpCodeDao;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public OtpExpirationScheduler(OtpCodeDao otpCodeDao) {
        this.otpCodeDao = otpCodeDao;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                otpCodeDao.expireOldCodes();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }
}