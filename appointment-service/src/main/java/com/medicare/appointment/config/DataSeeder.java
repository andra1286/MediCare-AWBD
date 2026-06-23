package com.medicare.appointment.config;

import com.medicare.appointment.domain.Appointment;
import com.medicare.appointment.domain.AppointmentStatus;
import com.medicare.appointment.repository.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;

/**
 * Demo appointments for doctor profile id=1 and patient profile id=1 (identity seed).
 * Skipped when data already exists or in the {@code test} profile.
 */
@Configuration
@Profile("!test")
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    /** Matches {@code DoctorProfile.id} from identity-service seed. */
    private static final long DEMO_DOCTOR_ID = 1L;
    /** Matches {@code PatientProfile.id} from identity-service seed. */
    private static final long DEMO_PATIENT_ID = 1L;

    @Bean
    CommandLineRunner seedAppointments(AppointmentRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            LocalDateTime now = LocalDateTime.now().withNano(0);

            Appointment completed = repository.save(Appointment.builder()
                    .doctorId(DEMO_DOCTOR_ID)
                    .patientId(DEMO_PATIENT_ID)
                    .scheduledAt(now.minusDays(7))
                    .status(AppointmentStatus.COMPLETED)
                    .reason("Annual checkup")
                    .build());

            repository.save(Appointment.builder()
                    .doctorId(DEMO_DOCTOR_ID)
                    .patientId(DEMO_PATIENT_ID)
                    .scheduledAt(now.plusDays(3))
                    .status(AppointmentStatus.BOOKED)
                    .reason("Follow-up consultation")
                    .build());

            repository.save(Appointment.builder()
                    .doctorId(DEMO_DOCTOR_ID)
                    .patientId(DEMO_PATIENT_ID)
                    .scheduledAt(now.plusDays(5))
                    .status(AppointmentStatus.BOOKED)
                    .reason("Blood test results review")
                    .build());

            repository.save(Appointment.builder()
                    .doctorId(DEMO_DOCTOR_ID)
                    .patientId(DEMO_PATIENT_ID)
                    .scheduledAt(now.plusDays(2))
                    .status(AppointmentStatus.CANCELLED)
                    .reason("Cancelled — patient rescheduled")
                    .build());

            log.info("Seeded 4 demo appointments (completed id={})", completed.getId());
        };
    }
}
