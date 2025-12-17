package com.example.passwordmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class EncryptionConfig {

    @Value("${encryption.key:dev-default-32-char-key-for-aes!}")
    private String encryptionKey;

    @Bean
    public String getEncryptionKey() {
        return encryptionKey;
    }
}
