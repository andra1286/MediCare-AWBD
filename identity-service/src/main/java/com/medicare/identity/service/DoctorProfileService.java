package com.medicare.identity.service;

import com.medicare.common.dto.DoctorDto;
import com.medicare.common.dto.PageResponse;
import com.medicare.common.exception.BusinessRuleException;
import com.medicare.common.exception.DuplicateResourceException;
import com.medicare.common.exception.ResourceNotFoundException;
import com.medicare.identity.domain.DoctorProfile;
import com.medicare.identity.domain.Role;
import com.medicare.identity.domain.User;
import com.medicare.identity.dto.CreateDoctorProfileRequest;
import com.medicare.identity.repository.DoctorProfileRepository;
import com.medicare.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DoctorProfileService {

    private static final String ROLE_DOCTOR = "ROLE_DOCTOR";

    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;
    private final IdentityMapper mapper;

    @Transactional(readOnly = true)
    public PageResponse<DoctorDto> list(Pageable pageable) {
        Page<DoctorDto> page = doctorProfileRepository.findAll(pageable).map(mapper::toDoctorDto);
        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public DoctorDto getById(Long id) {
        return mapper.toDoctorDto(findProfile(id));
    }

    /** Lookup by profile id, falling back to user id (for Feign callers using either). */
    @Transactional(readOnly = true)
    public DoctorDto getByIdOrUserId(Long id) {
        return doctorProfileRepository.findById(id)
                .or(() -> doctorProfileRepository.findByUserId(id))
                .map(mapper::toDoctorDto)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + id));
    }

    @Transactional
    public DoctorDto create(CreateDoctorProfileRequest request) {
        if (doctorProfileRepository.existsByLicenseNo(request.getLicenseNo())) {
            throw new DuplicateResourceException("License number already registered");
        }
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));
        ensureCanBecomeDoctor(user);
        if (!user.getRoles().stream().map(Role::getName).anyMatch(ROLE_DOCTOR::equals)) {
            throw new BusinessRuleException("User must have ROLE_DOCTOR before creating a doctor profile");
        }
        DoctorProfile profile = DoctorProfile.builder()
                .user(user)
                .specialty(request.getSpecialty())
                .licenseNo(request.getLicenseNo())
                .build();
        return mapper.toDoctorDto(doctorProfileRepository.save(profile));
    }

    @Transactional
    public void delete(Long id) {
        doctorProfileRepository.delete(findProfile(id));
    }

    private void ensureCanBecomeDoctor(User user) {
        if (user.getDoctorProfile() != null) {
            throw new BusinessRuleException("User already has a doctor profile");
        }
        if (user.getPatientProfile() != null) {
            throw new BusinessRuleException("A patient cannot also be a doctor");
        }
    }

    private DoctorProfile findProfile(Long id) {
        return doctorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + id));
    }
}
