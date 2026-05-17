package ru.mephi.sirnya;

import ru.mephi.sirnya.config.DatabaseConnector;
import ru.mephi.sirnya.controller.ApiServer;
import ru.mephi.sirnya.dao.OtpCodeDao;
import ru.mephi.sirnya.scheduler.OtpExpirationScheduler;

public class App {
    public static void main(String[] args) throws Exception {
        ApiServer server = new ApiServer();
        server.start();

        DatabaseConnector connector = new DatabaseConnector();
        OtpCodeDao otpCodeDao = new OtpCodeDao(connector);
        OtpExpirationScheduler expirationScheduler = new OtpExpirationScheduler(otpCodeDao);
        expirationScheduler.start();

    }
}