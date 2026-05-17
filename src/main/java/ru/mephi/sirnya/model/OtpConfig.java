package ru.mephi.sirnya.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpConfig {
    private int codeLength;
    private int lifetimeSeconds;
}
