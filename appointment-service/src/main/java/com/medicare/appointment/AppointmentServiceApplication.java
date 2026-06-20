package com.medicare.appointment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Entry point for appointment-service.
 * - scanBasePackages includes "com.medicare" so shared beans from common-lib load.
 * - @EnableFeignClients scans common-lib for the Feign interfaces (IdentityClient).
 * - @EnableCaching turns on the Redis-backed cache abstraction.
 */
@SpringBootApplication(scanBasePackages = "com.medicare")
@EnableFeignClients(basePackages = "com.medicare.common.client")
@EnableCaching
public class AppointmentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppointmentServiceApplication.class, args);
    }
}
