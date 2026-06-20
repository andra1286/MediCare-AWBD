package com.medicare.webui.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Form-backing object for the medication catalog. */
@Data
public class MedicationForm {

    @NotBlank(message = "Name is required")
    @Size(max = 150)
    private String name;

    @Size(max = 100)
    private String dosageForm;

    @Size(max = 20)
    private String atcCode;
}
