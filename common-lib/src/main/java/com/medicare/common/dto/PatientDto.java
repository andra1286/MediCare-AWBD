package com.medicare.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Patient profile data shared by identity-service (used when validating/booking). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientDto {
    private Long id;
    private Long userId;
    private String fullName;
    private String personalId;
    private String phone;
}
