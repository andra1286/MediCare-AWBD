package com.medicare.webui.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/** Form-backing object for booking an appointment (server-side validated). */
@Data
public class AppointmentForm {

    @NotNull(message = "Please choose a doctor")
    private Long doctorId;

    @NotNull(message = "Please choose a patient")
    private Long patientId;

    @NotNull(message = "Please pick a date and time")
    @Future(message = "The appointment must be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime scheduledAt;

    @Size(max = 500, message = "Reason must be at most 500 characters")
    private String reason;
}
