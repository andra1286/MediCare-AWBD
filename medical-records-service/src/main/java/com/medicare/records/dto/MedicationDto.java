package com.medicare.records.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response view of a medication. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicationDto {
    private Long id;
    private String name;
    private String dosageForm;
    private String atcCode;
}
