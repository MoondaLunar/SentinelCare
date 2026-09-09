package com.sentinelcare.controller;

import com.sentinelcare.entity.Patient;
import com.sentinelcare.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PatientController {

    /**
     * Service layer keeps controller logic thin and isolates persistence details.
     * The controller is intentionally focused on HTTP concerns rather than data access.
     */
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/patients")
    public List<Patient> getAllPatients() {
        String username = SecurityContextHolder.getContext().getAuthentication() != null
            ? SecurityContextHolder.getContext().getAuthentication().getName()
            : null;
        return patientService.getVisiblePatients(username);
    }

    @GetMapping("/patients/{id}")
    public ResponseEntity<Patient> getPatient(@PathVariable Long id) {
        if (!patientService.canAccessPatient(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return patientService.getPatient(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/patients")
    public Patient createPatient(@Valid @RequestBody Patient patient) {
        return patientService.createPatient(patient);
    }
}
