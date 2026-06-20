package com.medicare.webui.client;

import com.medicare.common.dto.PageResponse;
import com.medicare.webui.dto.MedicalRecordForm;
import com.medicare.webui.dto.MedicalRecordView;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Calls the medical-records endpoints of medical-records-service. */
@Component
public class MedicalRecordApiClient {

    private final RestClient client;

    public MedicalRecordApiClient(@Qualifier("recordsRestClient") RestClient client) {
        this.client = client;
    }

    public PageResponse<MedicalRecordView> list(int page, int size, String sort) {
        return client.get()
                .uri(b -> b.path("/api/records")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .queryParam("sort", sort)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<PageResponse<MedicalRecordView>>() {});
    }

    public MedicalRecordView get(Long id) {
        return client.get().uri("/api/records/{id}", id)
                .retrieve().body(MedicalRecordView.class);
    }

    /** Builds the create-record payload (with an optional single prescription) and posts it. */
    public MedicalRecordView create(MedicalRecordForm form) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("appointmentId", form.getAppointmentId());
        payload.put("doctorId", form.getDoctorId());
        payload.put("patientId", form.getPatientId());
        payload.put("diagnosis", form.getDiagnosis());
        payload.put("notes", form.getNotes());

        List<Map<String, Object>> prescriptions = new ArrayList<>();
        if (form.getMedicationIds() != null && !form.getMedicationIds().isEmpty()) {
            Map<String, Object> prescription = new HashMap<>();
            prescription.put("instructions", form.getPrescriptionInstructions());
            prescription.put("medicationIds", form.getMedicationIds());
            prescriptions.add(prescription);
        }
        payload.put("prescriptions", prescriptions);

        return client.post().uri("/api/records")
                .body(payload).retrieve().body(MedicalRecordView.class);
    }
}
