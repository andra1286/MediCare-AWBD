package com.medicare.webui.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PatientProfileForm {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotBlank
    @Size(max = 20)
    private String personalId;

    @Size(max = 20)
    private String phone;
}
