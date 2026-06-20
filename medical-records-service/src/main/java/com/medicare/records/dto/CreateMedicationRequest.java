package com.medicare.records.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Payload for adding/updating a medication in the catalog. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMedicationRequest {

    @NotBlank(message = "name is required")
    @Size(max = 150)
    private String name;

    @Size(max = 100)
    private String dosageForm;

    @Size(max = 20)
    private String atcCode;
}
