package com.medicare.appointment.domain;

/** Lifecycle of an appointment (rule BR-11). BOOKED is the only non-terminal state. */
public enum AppointmentStatus {
    BOOKED,
    CANCELLED,
    COMPLETED
}
