package com.medicare.webui;

import com.medicare.common.dto.AppointmentDto;
import com.medicare.common.dto.DoctorDto;
import com.medicare.common.dto.PageResponse;
import com.medicare.webui.client.AppointmentApiClient;
import com.medicare.webui.client.IdentityApiClient;
import com.medicare.webui.client.MedicalRecordApiClient;
import com.medicare.webui.client.MedicationApiClient;
import com.medicare.webui.dto.MedicationView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Renders the list/form pages with mocked backend clients. This catches Thymeleaf
 * template errors (which only surface at render time) without needing live services.
 */
@SpringBootTest
@AutoConfigureMockMvc
class WebUiPagesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentApiClient appointmentApiClient;
    @MockBean
    private MedicationApiClient medicationApiClient;
    @MockBean
    private MedicalRecordApiClient medicalRecordApiClient;
    @MockBean
    private IdentityApiClient identityApiClient;

    private <T> PageResponse<T> emptyPage() {
        return new PageResponse<>(List.of(), 0, 10, 0, 0, true);
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void appointmentListRenders() throws Exception {
        when(appointmentApiClient.list(anyInt(), anyInt(), anyString()))
                .thenReturn(this.<AppointmentDto>emptyPage());
        mockMvc.perform(get("/appointments")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void appointmentFormRenders() throws Exception {
        when(identityApiClient.listDoctorsForSelect()).thenReturn(List.of(
                new DoctorDto(1L, 2L, "Dr. Ana Popescu", "General Medicine", "DOC-001")));
        when(identityApiClient.listPatientsForSelect()).thenReturn(List.of());
        mockMvc.perform(get("/appointments/new")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void medicationListRenders() throws Exception {
        when(medicationApiClient.list(anyInt(), anyInt(), anyString()))
                .thenReturn(this.<MedicationView>emptyPage());
        mockMvc.perform(get("/medications")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void medicationFormRenders() throws Exception {
        mockMvc.perform(get("/medications/new")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void recordListRenders() throws Exception {
        when(medicalRecordApiClient.list(anyInt(), anyInt(), anyString())).thenReturn(emptyPage());
        mockMvc.perform(get("/records")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void recordFormRenders() throws Exception {
        when(medicationApiClient.listAll()).thenReturn(List.of());
        mockMvc.perform(get("/records/new")).andExpect(status().isOk());
    }
}
