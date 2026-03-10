package com.ecobank.fundtransferservice.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {

    private String secret;
    private long expiration = 86400000; // 24 hours in milliseconds
    private String issuer = "fund-transfer-service";

    @PostConstruct
    public void validate() {
        if (secret == null || secret.length() < 32) {
            log.warn("JWT secret is not set or is too short (minimum 32 characters). " +
                    "Please set JWT_SECRET environment variable with a strong secret key for production.");
            if (secret == null) {
                secret = "default-secret-key";
            }
        }
    }
}
