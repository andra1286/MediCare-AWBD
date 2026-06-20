package com.medicare.webui;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the Spring Security wiring: login is public, the rest is protected,
 * and an authenticated user reaches the dashboard. Also proves the context loads.
 */
@SpringBootTest
@AutoConfigureMockMvc
class WebUiSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginPageIsPublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    void dashboardRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void authenticatedUserReachesDashboard() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "patient", roles = "PATIENT")
    void patientCannotOpenNewMedicationForm() throws Exception {
        mockMvc.perform(get("/medications/new"))
                .andExpect(status().isForbidden());
    }
}
