package com.medicare.records;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests for records + medications (HTTP -> service -> JPA -> H2).
 * appointment-service is unreachable in tests, so record creation uses the resilience fail-open path.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecordsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createMedicationThenRecordWithPrescription() throws Exception {
        // 1) create a medication
        String medResponse = mockMvc.perform(post("/api/medications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "Amoxicillin", "dosageForm", "Capsule", "atcCode", "J01CA04"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long medId = objectMapper.readTree(medResponse).get("id").asLong();

        // 2) create a record with a prescription referencing that medication (@ManyToMany)
        Map<String, Object> prescription = Map.of(
                "instructions", "Twice a day", "medicationIds", List.of(medId));
        Map<String, Object> record = Map.of(
                "appointmentId", 500L, "doctorId", 10L, "patientId", 20L,
                "diagnosis", "Bacterial infection", "notes", "7 days",
                "prescriptions", List.of(prescription));

        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(record)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.diagnosis").value("Bacterial infection"))
                .andExpect(jsonPath("$.prescriptions[0].medications[0].name").value("Amoxicillin"));
    }

    @Test
    void medicationListIsPaginated() throws Exception {
        mockMvc.perform(post("/api/medications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "Paracetamol"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/medications")
                        .param("page", "0").param("size", "5").param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content").isArray());
    }
}
