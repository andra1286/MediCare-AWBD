package com.medicare.identity.repository;

import com.medicare.identity.domain.PatientProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientProfileRepository extends JpaRepository<PatientProfile, Long> {

    Optional<PatientProfile> findByUserId(Long userId);

    boolean existsByPersonalId(String personalId);
}
