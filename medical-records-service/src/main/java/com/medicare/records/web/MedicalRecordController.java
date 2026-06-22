package com.medicare.records.web;

import com.medicare.common.dto.PageResponse;
import com.medicare.records.dto.CreateMedicalRecordRequest;
import com.medicare.records.dto.MedicalRecordDto;
import com.medicare.records.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST API for medical records. */
@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<MedicalRecordDto> create(@Valid @RequestBody CreateMedicalRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public MedicalRecordDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    /** Paginated + sortable (e.g. ?page=0&size=10&sort=createdAt,desc&sort=diagnosis). */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public PageResponse<MedicalRecordDto> list(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public PageResponse<MedicalRecordDto> listByPatient(
            @PathVariable Long patientId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return service.listByPatient(patientId, pageable);
    }
}
