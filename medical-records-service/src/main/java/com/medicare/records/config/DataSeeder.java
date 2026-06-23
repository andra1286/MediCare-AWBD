package com.medicare.records.config;

import com.medicare.records.domain.MedicalRecord;
import com.medicare.records.domain.Medication;
import com.medicare.records.domain.Prescription;
import com.medicare.records.repository.MedicalRecordRepository;
import com.medicare.records.repository.MedicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.HashSet;
import java.util.Set;

/**
 * Demo medication catalog, one medical record and prescriptions.
 * Uses appointment id=1 (first completed appointment from appointment-service seed).
 */
@Configuration
@Profile("!test")
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private static final long DEMO_APPOINTMENT_ID = 1L;
    private static final long DEMO_DOCTOR_ID = 1L;
    private static final long DEMO_PATIENT_ID = 1L;

    @Bean
    CommandLineRunner seedRecords(MedicationRepository medicationRepository,
                                  MedicalRecordRepository medicalRecordRepository) {
        return args -> {
            seedMedications(medicationRepository);
            seedMedicalRecord(medicationRepository, medicalRecordRepository);
        };
    }

    private void seedMedications(MedicationRepository repository) {
        if (repository.count() > 0) {
            return;
        }
        saveMedication(repository, "Paracetamol", "Tablet 500mg", "N02BE01");
        saveMedication(repository, "Ibuprofen", "Tablet 400mg", "M01AE01");
        saveMedication(repository, "Amoxicillin", "Capsule 500mg", "J01CA04");
        saveMedication(repository, "Omeprazole", "Capsule 20mg", "A02BC01");
        log.info("Seeded 4 demo medications");
    }

    private void saveMedication(MedicationRepository repository, String name, String form, String atc) {
        repository.save(Medication.builder()
                .name(name)
                .dosageForm(form)
                .atcCode(atc)
                .build());
    }

    private void seedMedicalRecord(MedicationRepository medicationRepository,
                                   MedicalRecordRepository medicalRecordRepository) {
        if (medicalRecordRepository.count() > 0) {
            return;
        }
        Medication paracetamol = medicationRepository.findAll().stream()
                .filter(m -> "Paracetamol".equals(m.getName()))
                .findFirst()
                .orElseThrow();
        Medication amoxicillin = medicationRepository.findAll().stream()
                .filter(m -> "Amoxicillin".equals(m.getName()))
                .findFirst()
                .orElseThrow();

        MedicalRecord record = MedicalRecord.builder()
                .appointmentId(DEMO_APPOINTMENT_ID)
                .doctorId(DEMO_DOCTOR_ID)
                .patientId(DEMO_PATIENT_ID)
                .diagnosis("Hypertension — controlled")
                .notes("Blood pressure within normal range. Continue lifestyle advice and follow-up in 3 months.")
                .build();

        Prescription prescription = Prescription.builder()
                .instructions("Paracetamol: 1 tablet every 8 hours if needed. Amoxicillin: 1 capsule every 12 hours for 5 days.")
                .medications(new HashSet<>(Set.of(paracetamol, amoxicillin)))
                .build();
        record.addPrescription(prescription);

        medicalRecordRepository.save(record);
        log.info("Seeded demo medical record for appointment id={}", DEMO_APPOINTMENT_ID);
    }
}
