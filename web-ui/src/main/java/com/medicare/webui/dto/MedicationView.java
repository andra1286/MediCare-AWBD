package com.medicare.webui.dto;

import lombok.Data;

/** Mirrors the MedicationDto JSON returned by medical-records-service. */
@Data
public class MedicationView {
    private Long id;
    private String name;
    private String dosageForm;
    private String atcCode;
}
