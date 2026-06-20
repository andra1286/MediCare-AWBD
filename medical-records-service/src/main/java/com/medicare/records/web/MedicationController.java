package com.medicare.records.web;

import com.medicare.common.dto.PageResponse;
import com.medicare.records.dto.CreateMedicationRequest;
import com.medicare.records.dto.MedicationDto;
import com.medicare.records.service.MedicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST API for the medication catalog. */
@RestController
@RequestMapping("/api/medications")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService service;

    @PostMapping
    public ResponseEntity<MedicationDto> create(@Valid @RequestBody CreateMedicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{id}")
    public MedicationDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    /** Whole catalog (cached in Redis) — handy for UI dropdowns. */
    @GetMapping("/all")
    public java.util.List<MedicationDto> listAll() {
        return service.listAll();
    }

    /** Paginated + sortable (e.g. ?page=0&size=10&sort=name,asc&sort=atcCode). */
    @GetMapping
    public PageResponse<MedicationDto> list(
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return service.list(pageable);
    }

    @PutMapping("/{id}")
    public MedicationDto update(@PathVariable Long id, @Valid @RequestBody CreateMedicationRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
