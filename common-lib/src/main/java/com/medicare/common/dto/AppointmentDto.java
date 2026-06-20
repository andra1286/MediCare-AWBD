package com.medicare.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Appointment view shared by appointment-service.
 * medical-records-service reads this to confirm an appointment is COMPLETED (rule BR-13).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDto {
    private Long id;
    private Long doctorId;
    private Long patientId;
    private LocalDateTime scheduledAt;
    private String status;
    private String reason;
}
