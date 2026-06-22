package com.medicare.appointment.web;

import com.medicare.appointment.dto.CreateAppointmentRequest;
import com.medicare.appointment.service.AppointmentService;
import com.medicare.common.dto.AppointmentDto;
import com.medicare.common.dto.PageResponse;
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

/** REST API for appointments. */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<AppointmentDto> book(@Valid @RequestBody CreateAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.book(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public AppointmentDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    /** Paginated + sortable (e.g. ?page=0&size=10&sort=scheduledAt,desc&sort=status). */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public PageResponse<AppointmentDto> list(
            @PageableDefault(size = 10, sort = "scheduledAt") Pageable pageable) {
        return service.list(pageable);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public AppointmentDto cancel(@PathVariable Long id) {
        return service.cancel(id);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public AppointmentDto complete(@PathVariable Long id) {
        return service.complete(id);
    }
}
