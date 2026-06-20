package com.medicare.records.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** Response view of a medical record with its prescriptions. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordDto {
    private Long id;
    private Long appointmentId;
    private Long doctorId;
    private Long patientId;
    private String diagnosis;
    private String notes;
    private LocalDateTime createdAt;
    private List<PrescriptionDto> prescriptions;
}
