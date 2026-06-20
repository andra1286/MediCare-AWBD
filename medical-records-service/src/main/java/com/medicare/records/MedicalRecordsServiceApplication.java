package com.medicare.records;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Entry point for medical-records-service.
 * - scanBasePackages includes "com.medicare" so shared beans from common-lib load.
 * - @EnableFeignClients scans common-lib for Feign interfaces (AppointmentClient, IdentityClient).
 * - @EnableCaching turns on the Redis-backed cache abstraction.
 */
@SpringBootApplication(scanBasePackages = "com.medicare")
@EnableFeignClients(basePackages = "com.medicare.common.client")
@EnableCaching
public class MedicalRecordsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MedicalRecordsServiceApplication.class, args);
    }
}
