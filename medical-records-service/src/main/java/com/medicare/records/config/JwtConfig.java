package com.medicare.records.config;

import com.medicare.common.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Bean
    public JwtUtil jwtUtil(
            @Value("${medicare.jwt.secret}") String secret,
            @Value("${medicare.jwt.expiration-ms}") long expirationMs) {
        return new JwtUtil(secret, expirationMs);
    }
}
