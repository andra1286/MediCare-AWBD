package com.medicare.records.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** Response view of a prescription with its medications. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionDto {
    private Long id;
    private String instructions;
    private LocalDateTime issuedAt;
    private List<MedicationDto> medications;
}
