package com.medicare.webui.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** Mirrors the MedicalRecordDto JSON returned by medical-records-service. */
@Data
public class MedicalRecordView {
    private Long id;
    private Long appointmentId;
    private Long doctorId;
    private Long patientId;
    private String diagnosis;
    private String notes;
    private LocalDateTime createdAt;
    private List<PrescriptionView> prescriptions;
}
