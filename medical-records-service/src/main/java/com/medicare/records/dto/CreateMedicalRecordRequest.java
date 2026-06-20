package com.medicare.records.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** Payload for creating a medical record together with its prescriptions. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMedicalRecordRequest {

    @NotNull(message = "appointmentId is required")
    private Long appointmentId;

    @NotNull(message = "doctorId is required")
    private Long doctorId;

    @NotNull(message = "patientId is required")
    private Long patientId;

    @NotBlank(message = "diagnosis is required")
    @Size(max = 1000)
    private String diagnosis;

    @Size(max = 2000)
    private String notes;

    /** Optional prescriptions; each one is validated (@Valid cascades). */
    @Valid
    private List<PrescriptionRequest> prescriptions = new ArrayList<>();
}
