package com.medicare.records.service;

import com.medicare.common.dto.PageResponse;
import com.medicare.common.exception.BusinessRuleException;
import com.medicare.common.exception.DuplicateResourceException;
import com.medicare.common.exception.ResourceNotFoundException;
import com.medicare.records.domain.MedicalRecord;
import com.medicare.records.domain.Medication;
import com.medicare.records.domain.Prescription;
import com.medicare.records.dto.CreateMedicalRecordRequest;
import com.medicare.records.dto.MedicalRecordDto;
import com.medicare.records.dto.PrescriptionRequest;
import com.medicare.records.repository.MedicalRecordRepository;
import com.medicare.records.repository.MedicationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Business logic for medical records and their prescriptions.
 * Enforces BR-14 (one record per appointment), BR-16 (>=1 medication per prescription),
 * BR-17 (medications must exist). BR-13 (appointment COMPLETED) is added in Phase 3 via Feign.
 */
@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private static final Logger log = LoggerFactory.getLogger(MedicalRecordService.class);

    private final MedicalRecordRepository recordRepository;
    private final MedicationRepository medicationRepository;

    @Transactional
    public MedicalRecordDto create(CreateMedicalRecordRequest request) {
        if (recordRepository.existsByAppointmentId(request.getAppointmentId())) {
            throw new DuplicateResourceException(
                    "A medical record already exists for appointment " + request.getAppointmentId());
        }

        MedicalRecord record = MedicalRecord.builder()
                .appointmentId(request.getAppointmentId())
                .doctorId(request.getDoctorId())
                .patientId(request.getPatientId())
                .diagnosis(request.getDiagnosis())
                .notes(request.getNotes())
                .build();

        for (PrescriptionRequest pr : request.getPrescriptions()) {
            record.addPrescription(buildPrescription(pr));
        }

        MedicalRecord saved = recordRepository.save(record);
        log.info("Created medical record {} for appointment {} with {} prescription(s)",
                saved.getId(), saved.getAppointmentId(), saved.getPrescriptions().size());
        return RecordMapper.toDto(saved);
    }

    private Prescription buildPrescription(PrescriptionRequest pr) {
        Set<Long> ids = pr.getMedicationIds();
        if (ids == null || ids.isEmpty()) {
            throw new BusinessRuleException("A prescription must contain at least one medication");
        }
        List<Medication> found = medicationRepository.findAllById(ids);
        if (found.size() != ids.size()) {
            throw new ResourceNotFoundException("One or more medications do not exist: " + ids);
        }
        return Prescription.builder()
                .instructions(pr.getInstructions())
                .medications(new HashSet<>(found))
                .build();
    }

    @Transactional(readOnly = true)
    public MedicalRecordDto getById(Long id) {
        return RecordMapper.toDto(findEntity(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<MedicalRecordDto> list(Pageable pageable) {
        Page<MedicalRecordDto> page = recordRepository.findAll(pageable).map(RecordMapper::toDto);
        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<MedicalRecordDto> listByPatient(Long patientId, Pageable pageable) {
        Page<MedicalRecordDto> page = recordRepository.findByPatientId(patientId, pageable)
                .map(RecordMapper::toDto);
        return PageResponse.from(page);
    }

    private MedicalRecord findEntity(Long id) {
        return recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord", id));
    }
}
