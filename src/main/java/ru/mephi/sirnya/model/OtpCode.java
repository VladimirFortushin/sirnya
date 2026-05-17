package ru.mephi.sirnya.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpCode {
    private int id;
    private int userId;
    private String operationId;
    private String code;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
