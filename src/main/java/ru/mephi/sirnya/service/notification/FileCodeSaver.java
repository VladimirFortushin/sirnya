package ru.mephi.sirnya.service.notification;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileCodeSaver {
    private final Path baseDir = Paths.get(System.getProperty("user.dir"));

    public void saveCode(String operationId, String code) {
        String filename = "otp_code_" + operationId + ".txt";
        Path filePath = baseDir.resolve(filename);
        try (PrintWriter out = new PrintWriter(new FileWriter(filePath.toFile()))) {
            out.println("OTP code: " + code);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save code to file", e);
        }
    }
}