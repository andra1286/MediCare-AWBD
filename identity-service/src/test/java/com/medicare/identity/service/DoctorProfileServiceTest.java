package com.medicare.identity.service;

import com.medicare.common.exception.BusinessRuleException;
import com.medicare.common.exception.DuplicateResourceException;
import com.medicare.identity.domain.DoctorProfile;
import com.medicare.identity.domain.Role;
import com.medicare.identity.domain.User;
import com.medicare.identity.dto.CreateDoctorProfileRequest;
import com.medicare.identity.repository.DoctorProfileRepository;
import com.medicare.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorProfileServiceTest {

    @Mock
    private DoctorProfileRepository doctorProfileRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private IdentityMapper mapper;

    @InjectMocks
    private DoctorProfileService doctorProfileService;

    @Test
    void createRejectsUserWhoIsAlreadyPatient() {
        User user = User.builder()
                .id(1L)
                .patientProfile(new com.medicare.identity.domain.PatientProfile())
                .roles(Set.of(Role.builder().name("ROLE_DOCTOR").build()))
                .build();
        CreateDoctorProfileRequest request = new CreateDoctorProfileRequest();
        request.setUserId(1L);
        request.setSpecialty("Cardiology");
        request.setLicenseNo("LIC-99");

        when(doctorProfileRepository.existsByLicenseNo("LIC-99")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> doctorProfileService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("patient");
    }

    @Test
    void createRejectsDuplicateLicense() {
        CreateDoctorProfileRequest request = new CreateDoctorProfileRequest();
        request.setLicenseNo("LIC-1");
        when(doctorProfileRepository.existsByLicenseNo("LIC-1")).thenReturn(true);

        assertThatThrownBy(() -> doctorProfileService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
    }
}
