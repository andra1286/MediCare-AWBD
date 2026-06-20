package com.medicare.appointment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: verifies the full Spring context starts with the H2 test profile.
 * Catches JPA mapping / bean wiring problems that unit tests don't.
 */
@SpringBootTest
@ActiveProfiles("test")
class AppointmentServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
