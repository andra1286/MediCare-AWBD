package com.medicare.appointment.service;

import com.medicare.appointment.domain.Appointment;
import com.medicare.appointment.domain.AppointmentStatus;
import com.medicare.appointment.dto.CreateAppointmentRequest;
import com.medicare.appointment.repository.AppointmentRepository;
import com.medicare.common.dto.AppointmentDto;
import com.medicare.common.dto.PageResponse;
import com.medicare.common.exception.BusinessRuleException;
import com.medicare.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Business logic for appointments. Enforces the booking rules from BUSINESS_RULES.md
 * and throws custom exceptions (mapped to HTTP statuses by the GlobalExceptionHandler).
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository repository;

    /** Book a new appointment (rules BR-8 no past date, BR-9 no double-booking). */
    @Transactional
    public AppointmentDto book(CreateAppointmentRequest request) {
        if (request.getScheduledAt().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("Cannot book an appointment in the past");
        }
        if (repository.existsByDoctorIdAndScheduledAt(request.getDoctorId(), request.getScheduledAt())) {
            throw new BusinessRuleException("Doctor already has an appointment at this time");
        }
        Appointment appointment = Appointment.builder()
                .doctorId(request.getDoctorId())
                .patientId(request.getPatientId())
                .scheduledAt(request.getScheduledAt())
                .reason(request.getReason())
                .status(AppointmentStatus.BOOKED)
                .build();
        Appointment saved = repository.save(appointment);
        log.info("Booked appointment {} for patient {} with doctor {}",
                saved.getId(), saved.getPatientId(), saved.getDoctorId());
        return toDto(saved);
    }

    /** Cancel an appointment (rule BR-11: only a BOOKED appointment can be cancelled). */
    @Transactional
    public AppointmentDto cancel(Long id) {
        Appointment appointment = findEntity(id);
        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new BusinessRuleException("Only a BOOKED appointment can be cancelled");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        log.info("Cancelled appointment {}", id);
        return toDto(repository.save(appointment));
    }

    /** Complete an appointment (rule BR-11: only a BOOKED appointment can be completed). */
    @Transactional
    public AppointmentDto complete(Long id) {
        Appointment appointment = findEntity(id);
        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new BusinessRuleException("Only a BOOKED appointment can be completed");
        }
        appointment.setStatus(AppointmentStatus.COMPLETED);
        log.info("Completed appointment {}", id);
        return toDto(repository.save(appointment));
    }

    @Transactional(readOnly = true)
    public AppointmentDto getById(Long id) {
        return toDto(findEntity(id));
    }

    /** Paginated, sortable list of all appointments (rule BR-18). */
    @Transactional(readOnly = true)
    public PageResponse<AppointmentDto> list(Pageable pageable) {
        Page<AppointmentDto> page = repository.findAll(pageable).map(this::toDto);
        return PageResponse.from(page);
    }

    private Appointment findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
    }

    private AppointmentDto toDto(Appointment a) {
        return new AppointmentDto(
                a.getId(), a.getDoctorId(), a.getPatientId(),
                a.getScheduledAt(), a.getStatus().name(), a.getReason());
    }
}
