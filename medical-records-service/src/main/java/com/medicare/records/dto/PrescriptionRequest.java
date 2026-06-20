package com.medicare.records.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/** One prescription to create inside a medical record. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionRequest {

    @Size(max = 500, message = "instructions must be at most 500 characters")
    private String instructions;

    /** At least one medication is required (rule BR-16). */
    @NotEmpty(message = "a prescription must contain at least one medication")
    private Set<Long> medicationIds;
}
