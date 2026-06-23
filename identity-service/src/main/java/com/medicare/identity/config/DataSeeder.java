package com.medicare.identity.config;

import com.medicare.identity.domain.DoctorProfile;
import com.medicare.identity.domain.PatientProfile;
import com.medicare.identity.domain.Role;
import com.medicare.identity.domain.User;
import com.medicare.identity.repository.DoctorProfileRepository;
import com.medicare.identity.repository.PatientProfileRepository;
import com.medicare.identity.repository.RoleRepository;
import com.medicare.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    CommandLineRunner seedIdentityData(
            RoleRepository roleRepository,
            UserRepository userRepository,
            DoctorProfileRepository doctorProfileRepository,
            PatientProfileRepository patientProfileRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            seedRole(roleRepository, "ROLE_ADMIN");
            seedRole(roleRepository, "ROLE_DOCTOR");
            seedRole(roleRepository, "ROLE_PATIENT");

            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = userRepository.save(User.builder()
                        .username("admin")
                        .email("admin@medicare.local")
                        .fullName("System Admin")
                        .passwordHash(passwordEncoder.encode("admin"))
                        .enabled(true)
                        .roles(Set.of(roleRepository.findByName("ROLE_ADMIN").orElseThrow()))
                        .build());
                log.info("Seeded admin user id={}", admin.getId());
            }

            if (userRepository.findByUsername("doctor").isEmpty()) {
                User doctorUser = userRepository.save(User.builder()
                        .username("doctor")
                        .email("doctor@medicare.local")
                        .fullName("Dr. Ana Popescu")
                        .passwordHash(passwordEncoder.encode("doctor"))
                        .enabled(true)
                        .roles(Set.of(roleRepository.findByName("ROLE_DOCTOR").orElseThrow()))
                        .build());
                DoctorProfile doctorProfile = doctorProfileRepository.save(DoctorProfile.builder()
                        .user(doctorUser)
                        .specialty("General Medicine")
                        .licenseNo("DOC-001")
                        .build());
                log.info("Seeded doctor user id={}, profile id={}", doctorUser.getId(), doctorProfile.getId());
            }

            if (userRepository.findByUsername("patient").isEmpty()) {
                User patientUser = userRepository.save(User.builder()
                        .username("patient")
                        .email("patient@medicare.local")
                        .fullName("Ion Ionescu")
                        .passwordHash(passwordEncoder.encode("patient"))
                        .enabled(true)
                        .roles(Set.of(roleRepository.findByName("ROLE_PATIENT").orElseThrow()))
                        .build());
                PatientProfile patientProfile = patientProfileRepository.save(PatientProfile.builder()
                        .user(patientUser)
                        .personalId("PAT-001")
                        .phone("0700000000")
                        .build());
                log.info("Seeded patient user id={}, profile id={}", patientUser.getId(), patientProfile.getId());
            }
        };
    }

    private void seedRole(RoleRepository roleRepository, String name) {
        if (roleRepository.findByName(name).isEmpty()) {
            roleRepository.save(Role.builder().name(name).build());
        }
    }
}
