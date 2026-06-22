package com.medicare.identity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicare.common.security.JwtUtil;
import com.medicare.identity.dto.LoginRequest;
import com.medicare.identity.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IdentityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void registerLoginThenAccessProtectedEndpoint() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setUsername("integration");
        register.setEmail("integration@test.local");
        register.setFullName("Integration Patient");
        register.setPassword("secret123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty());

        LoginRequest login = new LoginRequest();
        login.setUsername("integration");
        login.setPassword("secret123");

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("token").asText();
        assertThat(jwtUtil.isValid(token)).isTrue();

        mockMvc.perform(get("/api/doctors")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void internalDoctorLookupReturnsSeededDoctor() throws Exception {
        mockMvc.perform(get("/internal/doctors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Dr. Ana Popescu"));
    }
}
