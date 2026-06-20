package com.medicare.records.repository;

import com.medicare.records.domain.Medication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationRepository extends JpaRepository<Medication, Long> {

    boolean existsByName(String name);

    Page<Medication> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
