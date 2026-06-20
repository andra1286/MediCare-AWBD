package com.medicare.webui.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * Form-backing object for creating a medical record with a single prescription.
 * (The backend supports many prescriptions; the UI keeps it to one for simplicity.)
 */
@Data
public class MedicalRecordForm {

    @NotNull(message = "Appointment is required")
    private Long appointmentId;

    @NotNull(message = "Doctor is required")
    private Long doctorId;

    @NotNull(message = "Patient is required")
    private Long patientId;

    @NotBlank(message = "Diagnosis is required")
    @Size(max = 1000)
    private String diagnosis;

    @Size(max = 2000)
    private String notes;

    @Size(max = 500)
    private String prescriptionInstructions;

    /** Selected medication ids for the (optional) prescription. */
    private Set<Long> medicationIds = new HashSet<>();
}
