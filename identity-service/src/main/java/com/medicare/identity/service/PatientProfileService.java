package com.medicare.identity.service;

import com.medicare.common.dto.PageResponse;
import com.medicare.common.dto.PatientDto;
import com.medicare.common.exception.BusinessRuleException;
import com.medicare.common.exception.DuplicateResourceException;
import com.medicare.common.exception.ResourceNotFoundException;
import com.medicare.identity.domain.PatientProfile;
import com.medicare.identity.domain.Role;
import com.medicare.identity.domain.User;
import com.medicare.identity.dto.CreatePatientProfileRequest;
import com.medicare.identity.repository.PatientProfileRepository;
import com.medicare.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatientProfileService {

    private static final String ROLE_PATIENT = "ROLE_PATIENT";

    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;
    private final IdentityMapper mapper;

    @Transactional(readOnly = true)
    public PageResponse<PatientDto> list(Pageable pageable) {
        Page<PatientDto> page = patientProfileRepository.findAll(pageable).map(mapper::toPatientDto);
        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public PatientDto getById(Long id) {
        return mapper.toPatientDto(findProfile(id));
    }

    /** Lookup by profile id, falling back to user id (for Feign callers using either). */
    @Transactional(readOnly = true)
    public PatientDto getByIdOrUserId(Long id) {
        return patientProfileRepository.findById(id)
                .or(() -> patientProfileRepository.findByUserId(id))
                .map(mapper::toPatientDto)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + id));
    }

    @Transactional
    public PatientDto create(CreatePatientProfileRequest request) {
        if (patientProfileRepository.existsByPersonalId(request.getPersonalId())) {
            throw new DuplicateResourceException("Personal id already registered");
        }
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));
        ensureCanBecomePatient(user);
        if (!user.getRoles().stream().map(Role::getName).anyMatch(ROLE_PATIENT::equals)) {
            throw new BusinessRuleException("User must have ROLE_PATIENT before creating a patient profile");
        }
        PatientProfile profile = PatientProfile.builder()
                .user(user)
                .personalId(request.getPersonalId())
                .phone(request.getPhone())
                .build();
        return mapper.toPatientDto(patientProfileRepository.save(profile));
    }

    @Transactional
    public void delete(Long id) {
        patientProfileRepository.delete(findProfile(id));
    }

    private void ensureCanBecomePatient(User user) {
        if (user.getPatientProfile() != null) {
            throw new BusinessRuleException("User already has a patient profile");
        }
        if (user.getDoctorProfile() != null) {
            throw new BusinessRuleException("A doctor cannot also be a patient");
        }
    }

    private PatientProfile findProfile(Long id) {
        return patientProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + id));
    }
}
