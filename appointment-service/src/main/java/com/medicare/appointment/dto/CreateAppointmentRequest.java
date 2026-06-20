package com.medicare.appointment.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Payload for booking an appointment. Validated with Bean Validation before processing. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAppointmentRequest {

    @NotNull(message = "doctorId is required")
    private Long doctorId;

    @NotNull(message = "patientId is required")
    private Long patientId;

    @NotNull(message = "scheduledAt is required")
    @Future(message = "scheduledAt must be in the future")
    private LocalDateTime scheduledAt;

    @Size(max = 500, message = "reason must be at most 500 characters")
    private String reason;
}
