package com.medicare.records.service;

import com.medicare.common.exception.BusinessRuleException;
import com.medicare.common.exception.DuplicateResourceException;
import com.medicare.common.exception.ResourceNotFoundException;
import com.medicare.records.client.AppointmentGateway;
import com.medicare.records.domain.MedicalRecord;
import com.medicare.records.domain.Medication;
import com.medicare.records.dto.CreateMedicalRecordRequest;
import com.medicare.records.dto.MedicalRecordDto;
import com.medicare.records.dto.PrescriptionRequest;
import com.medicare.records.repository.MedicalRecordRepository;
import com.medicare.records.repository.MedicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {

    @Mock
    private MedicalRecordRepository recordRepository;

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private AppointmentGateway appointmentGateway;

    @InjectMocks
    private MedicalRecordService service;

    private CreateMedicalRecordRequest requestWithMedications(Set<Long> medicationIds) {
        CreateMedicalRecordRequest req = new CreateMedicalRecordRequest();
        req.setAppointmentId(100L);
        req.setDoctorId(10L);
        req.setPatientId(20L);
        req.setDiagnosis("Flu");
        req.setNotes("Rest and fluids");
        PrescriptionRequest pr = new PrescriptionRequest("Twice a day", medicationIds);
        req.setPrescriptions(List.of(pr));
        return req;
    }

    @Test
    void create_withValidPrescription_succeeds() {
        when(recordRepository.existsByAppointmentId(100L)).thenReturn(false);
        when(medicationRepository.findAllById(any()))
                .thenReturn(List.of(Medication.builder().id(1L).name("Aspirin").build()));
        when(recordRepository.save(any(MedicalRecord.class))).thenAnswer(inv -> {
            MedicalRecord r = inv.getArgument(0);
            r.setId(7L);
            return r;
        });

        MedicalRecordDto dto = service.create(requestWithMedications(Set.of(1L)));

        assertThat(dto.getId()).isEqualTo(7L);
        assertThat(dto.getPrescriptions()).hasSize(1);
        assertThat(dto.getPrescriptions().get(0).getMedications()).hasSize(1);
    }

    @Test
    void create_duplicateAppointment_throws() {
        when(recordRepository.existsByAppointmentId(100L)).thenReturn(true);

        assertThatThrownBy(() -> service.create(requestWithMedications(Set.of(1L))))
                .isInstanceOf(DuplicateResourceException.class);
        verify(recordRepository, never()).save(any());
    }

    @Test
    void create_withMissingMedication_throwsNotFound() {
        when(recordRepository.existsByAppointmentId(100L)).thenReturn(false);
        // ask for 2 ids but repository finds only 1
        when(medicationRepository.findAllById(any()))
                .thenReturn(List.of(Medication.builder().id(1L).name("Aspirin").build()));

        assertThatThrownBy(() -> service.create(requestWithMedications(Set.of(1L, 2L))))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(recordRepository, never()).save(any());
    }

    @Test
    void create_withEmptyMedications_throwsBusinessRule() {
        when(recordRepository.existsByAppointmentId(100L)).thenReturn(false);

        assertThatThrownBy(() -> service.create(requestWithMedications(Set.of())))
                .isInstanceOf(BusinessRuleException.class);
        verify(recordRepository, never()).save(any());
    }

    @Test
    void getById_missing_throwsNotFound() {
        when(recordRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
