package com.medicare.identity.web;

import com.medicare.common.dto.DoctorDto;
import com.medicare.common.dto.PatientDto;
import com.medicare.common.dto.UserDto;
import com.medicare.identity.service.DoctorProfileService;
import com.medicare.identity.service.PatientProfileService;
import com.medicare.identity.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contract consumed by {@code IdentityClient} in common-lib (Feign, path /internal).
 */
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {

    private final DoctorProfileService doctorProfileService;
    private final PatientProfileService patientProfileService;
    private final UserService userService;

    @GetMapping("/doctors/{id}")
    public DoctorDto getDoctor(@PathVariable Long id) {
        return doctorProfileService.getByIdOrUserId(id);
    }

    @GetMapping("/patients/{id}")
    public PatientDto getPatient(@PathVariable Long id) {
        return patientProfileService.getByIdOrUserId(id);
    }

    @GetMapping("/users/{id}")
    public UserDto getUser(@PathVariable Long id) {
        return userService.getById(id);
    }
}
