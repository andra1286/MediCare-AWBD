package com.medicare.records;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Smoke test: full context starts with the H2 test profile (validates JPA mappings). */
@SpringBootTest
@ActiveProfiles("test")
class MedicalRecordsServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
