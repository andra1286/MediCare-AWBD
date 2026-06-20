package com.medicare.records.service;

import com.medicare.common.dto.PageResponse;
import com.medicare.common.exception.DuplicateResourceException;
import com.medicare.common.exception.ResourceNotFoundException;
import com.medicare.records.domain.Medication;
import com.medicare.records.dto.CreateMedicationRequest;
import com.medicare.records.dto.MedicationDto;
import com.medicare.records.repository.MedicationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CRUD for the medication catalog. */
@Service
@RequiredArgsConstructor
public class MedicationService {

    private static final Logger log = LoggerFactory.getLogger(MedicationService.class);

    private final MedicationRepository repository;

    @Transactional
    public MedicationDto create(CreateMedicationRequest request) {
        if (repository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Medication already exists: " + request.getName());
        }
        Medication saved = repository.save(Medication.builder()
                .name(request.getName())
                .dosageForm(request.getDosageForm())
                .atcCode(request.getAtcCode())
                .build());
        log.info("Created medication {} ({})", saved.getId(), saved.getName());
        return RecordMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public MedicationDto getById(Long id) {
        return RecordMapper.toDto(findEntity(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<MedicationDto> list(Pageable pageable) {
        Page<MedicationDto> page = repository.findAll(pageable).map(RecordMapper::toDto);
        return PageResponse.from(page);
    }

    @Transactional
    public MedicationDto update(Long id, CreateMedicationRequest request) {
        Medication medication = findEntity(id);
        medication.setName(request.getName());
        medication.setDosageForm(request.getDosageForm());
        medication.setAtcCode(request.getAtcCode());
        return RecordMapper.toDto(repository.save(medication));
    }

    @Transactional
    public void delete(Long id) {
        Medication medication = findEntity(id);
        repository.delete(medication);
        log.info("Deleted medication {}", id);
    }

    private Medication findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medication", id));
    }
}
