package com.medicare.appointment.repository;

import com.medicare.appointment.domain.Appointment;
import com.medicare.appointment.domain.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

/**
 * Data access for appointments. JpaRepository provides CRUD + pagination/sorting;
 * the methods below are derived queries Spring Data implements from their names.
 */
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /** Used to enforce BR-9 (no double-booking the same doctor at the same time). */
    boolean existsByDoctorIdAndScheduledAt(Long doctorId, LocalDateTime scheduledAt);

    Page<Appointment> findByPatientId(Long patientId, Pageable pageable);

    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);

    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);
}
