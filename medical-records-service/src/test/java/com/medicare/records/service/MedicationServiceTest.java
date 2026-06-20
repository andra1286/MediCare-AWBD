package com.medicare.records.service;

import com.medicare.common.exception.DuplicateResourceException;
import com.medicare.common.exception.ResourceNotFoundException;
import com.medicare.records.domain.Medication;
import com.medicare.records.dto.CreateMedicationRequest;
import com.medicare.records.dto.MedicationDto;
import com.medicare.records.repository.MedicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicationServiceTest {

    @Mock
    private MedicationRepository repository;

    @InjectMocks
    private MedicationService service;

    @Test
    void create_savesNewMedication() {
        when(repository.existsByName("Amoxicillin")).thenReturn(false);
        when(repository.save(any(Medication.class))).thenAnswer(inv -> {
            Medication m = inv.getArgument(0);
            m.setId(5L);
            return m;
        });

        MedicationDto dto = service.create(new CreateMedicationRequest("Amoxicillin", "Capsule", "J01CA04"));

        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getName()).isEqualTo("Amoxicillin");
    }

    @Test
    void create_duplicateName_throws() {
        when(repository.existsByName("Aspirin")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateMedicationRequest("Aspirin", "Tablet", "N02BA01")))
                .isInstanceOf(DuplicateResourceException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void getById_missing_throwsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_existing_removes() {
        Medication med = Medication.builder().id(1L).name("Aspirin").build();
        when(repository.findById(1L)).thenReturn(Optional.of(med));

        service.delete(1L);

        verify(repository).delete(med);
    }
}
