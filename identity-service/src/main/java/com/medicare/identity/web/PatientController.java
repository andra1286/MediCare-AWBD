package com.medicare.identity.web;

import com.medicare.common.dto.PageResponse;
import com.medicare.common.dto.PatientDto;
import com.medicare.identity.dto.CreatePatientProfileRequest;
import com.medicare.identity.service.PatientProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientProfileService patientProfileService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public PageResponse<PatientDto> list(@PageableDefault(size = 10, sort = "personalId") Pageable pageable) {
        return patientProfileService.list(pageable);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public PatientDto me(@AuthenticationPrincipal UserDetails user) {
        return patientProfileService.getByUsername(user.getUsername());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public PatientDto getById(@PathVariable Long id) {
        return patientProfileService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PatientDto> create(@Valid @RequestBody CreatePatientProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientProfileService.create(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        patientProfileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
