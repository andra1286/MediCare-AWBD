package com.medicare.appointment.service;

import com.medicare.appointment.client.IdentityGateway;
import com.medicare.appointment.domain.Appointment;
import com.medicare.appointment.domain.AppointmentStatus;
import com.medicare.appointment.dto.CreateAppointmentRequest;
import com.medicare.appointment.repository.AppointmentRepository;
import com.medicare.common.dto.AppointmentDto;
import com.medicare.common.exception.BusinessRuleException;
import com.medicare.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository repository;

    @Mock
    private IdentityGateway identityGateway;

    @InjectMocks
    private AppointmentService service;

    private CreateAppointmentRequest validRequest() {
        return new CreateAppointmentRequest(10L, 20L,
                LocalDateTime.now().plusDays(1), "Routine check");
    }

    private Appointment booked(Long id) {
        return Appointment.builder()
                .id(id).doctorId(10L).patientId(20L)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .status(AppointmentStatus.BOOKED).reason("Routine check")
                .build();
    }

    @Test
    void book_savesBookedAppointment() {
        when(repository.existsByDoctorIdAndScheduledAt(any(), any())).thenReturn(false);
        when(repository.save(any(Appointment.class))).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        AppointmentDto dto = service.book(validRequest());

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStatus()).isEqualTo("BOOKED");
        assertThat(dto.getDoctorId()).isEqualTo(10L);
    }

    @Test
    void book_inThePast_throws() {
        CreateAppointmentRequest past = new CreateAppointmentRequest(10L, 20L,
                LocalDateTime.now().minusDays(1), "late");

        assertThatThrownBy(() -> service.book(past))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("past");
        verify(repository, never()).save(any());
    }

    @Test
    void book_doubleBooking_throws() {
        when(repository.existsByDoctorIdAndScheduledAt(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> service.book(validRequest()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already has an appointment");
        verify(repository, never()).save(any());
    }

    @Test
    void cancel_bookedAppointment_setsCancelled() {
        when(repository.findById(1L)).thenReturn(Optional.of(booked(1L)));
        when(repository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        AppointmentDto dto = service.cancel(1L);

        assertThat(dto.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void cancel_nonBooked_throws() {
        Appointment completed = booked(1L);
        completed.setStatus(AppointmentStatus.COMPLETED);
        when(repository.findById(1L)).thenReturn(Optional.of(completed));

        assertThatThrownBy(() -> service.cancel(1L))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void complete_bookedAppointment_setsCompleted() {
        when(repository.findById(1L)).thenReturn(Optional.of(booked(1L)));
        when(repository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        AppointmentDto dto = service.complete(1L);

        assertThat(dto.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void getById_missing_throwsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
