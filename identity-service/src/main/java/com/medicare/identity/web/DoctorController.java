package com.medicare.identity.web;

import com.medicare.common.dto.DoctorDto;
import com.medicare.common.dto.PageResponse;
import com.medicare.identity.dto.CreateDoctorProfileRequest;
import com.medicare.identity.service.DoctorProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorProfileService doctorProfileService;

    @GetMapping
    public PageResponse<DoctorDto> list(@PageableDefault(size = 10, sort = "specialty") Pageable pageable) {
        return doctorProfileService.list(pageable);
    }

    @GetMapping("/{id}")
    public DoctorDto getById(@PathVariable Long id) {
        return doctorProfileService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorDto> create(@Valid @RequestBody CreateDoctorProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorProfileService.create(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        doctorProfileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
