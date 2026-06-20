package com.medicare.appointment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for appointment-service.
 * scanBasePackages includes "com.medicare" so shared beans from common-lib
 * (e.g. the GlobalExceptionHandler) are picked up.
 */
@SpringBootApplication(scanBasePackages = "com.medicare")
public class AppointmentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppointmentServiceApplication.class, args);
    }
}
