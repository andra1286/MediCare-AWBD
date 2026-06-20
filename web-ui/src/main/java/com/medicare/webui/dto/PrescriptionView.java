package com.medicare.webui.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** Mirrors the PrescriptionDto JSON returned by medical-records-service. */
@Data
public class PrescriptionView {
    private Long id;
    private String instructions;
    private LocalDateTime issuedAt;
    private List<MedicationView> medications;
}
