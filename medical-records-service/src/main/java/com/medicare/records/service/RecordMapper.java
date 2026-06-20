package com.medicare.records.service;

import com.medicare.records.domain.MedicalRecord;
import com.medicare.records.domain.Medication;
import com.medicare.records.domain.Prescription;
import com.medicare.records.dto.MedicalRecordDto;
import com.medicare.records.dto.MedicationDto;
import com.medicare.records.dto.PrescriptionDto;

import java.util.List;

/** Converts JPA entities to response DTOs. */
final class RecordMapper {

    private RecordMapper() {
    }

    static MedicationDto toDto(Medication m) {
        return new MedicationDto(m.getId(), m.getName(), m.getDosageForm(), m.getAtcCode());
    }

    static PrescriptionDto toDto(Prescription p) {
        List<MedicationDto> meds = p.getMedications().stream()
                .map(RecordMapper::toDto)
                .toList();
        return new PrescriptionDto(p.getId(), p.getInstructions(), p.getIssuedAt(), meds);
    }

    static MedicalRecordDto toDto(MedicalRecord r) {
        List<PrescriptionDto> prescriptions = r.getPrescriptions().stream()
                .map(RecordMapper::toDto)
                .toList();
        return new MedicalRecordDto(
                r.getId(), r.getAppointmentId(), r.getDoctorId(), r.getPatientId(),
                r.getDiagnosis(), r.getNotes(), r.getCreatedAt(), prescriptions);
    }
}
