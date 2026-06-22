package com.medicare.identity.service;

import com.medicare.common.dto.DoctorDto;
import com.medicare.common.dto.PatientDto;
import com.medicare.common.dto.UserDto;
import com.medicare.identity.domain.DoctorProfile;
import com.medicare.identity.domain.PatientProfile;
import com.medicare.identity.domain.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IdentityMapper {

    public UserDto toUserDto(User user) {
        List<String> roles = user.getRoles().stream().map(r -> r.getName()).toList();
        return new UserDto(user.getId(), user.getUsername(), user.getEmail(), roles, user.isEnabled());
    }

    public DoctorDto toDoctorDto(DoctorProfile profile) {
        User user = profile.getUser();
        return new DoctorDto(
                profile.getId(),
                user.getId(),
                user.getFullName(),
                profile.getSpecialty(),
                profile.getLicenseNo());
    }

    public PatientDto toPatientDto(PatientProfile profile) {
        User user = profile.getUser();
        return new PatientDto(
                profile.getId(),
                user.getId(),
                user.getFullName(),
                profile.getPersonalId(),
                profile.getPhone());
    }
}
