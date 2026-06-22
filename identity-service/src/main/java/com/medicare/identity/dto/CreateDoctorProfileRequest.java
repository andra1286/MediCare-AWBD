package com.medicare.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateDoctorProfileRequest {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotBlank
    @Size(max = 100)
    private String specialty;

    @NotBlank
    @Size(max = 50)
    private String licenseNo;
}
