package com.medicare.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Doctor profile data shared by identity-service (used when validating/booking). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDto {
    private Long id;
    private Long userId;
    private String fullName;
    private String specialty;
    private String licenseNo;
}
