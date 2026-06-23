package com.medicare.identity.repository;

import com.medicare.identity.domain.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {

    Optional<DoctorProfile> findByUserId(Long userId);

    boolean existsByLicenseNo(String licenseNo);
}
