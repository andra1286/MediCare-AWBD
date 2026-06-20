package com.medicare.records;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for medical-records-service.
 * scanBasePackages includes "com.medicare" so shared beans from common-lib load.
 */
@SpringBootApplication(scanBasePackages = "com.medicare")
public class MedicalRecordsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MedicalRecordsServiceApplication.class, args);
    }
}
