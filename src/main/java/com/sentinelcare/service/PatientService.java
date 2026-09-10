package com.sentinelcare.service;

import com.sentinelcare.entity.Patient;
import com.sentinelcare.repository.PatientRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientAuthorizationService patientAuthorizationService;

    public PatientService(PatientRepository patientRepository, PatientAuthorizationService patientAuthorizationService) {
        this.patientRepository = patientRepository;
        this.patientAuthorizationService = patientAuthorizationService;
    }

    public List<Patient> getAllPatients() {
        return getVisiblePatients(patientAuthorizationService.currentUsername());
    }

    public List<Patient> getVisiblePatients(String username) {
        if (username == null || username.isBlank()) {
            return List.of();
        }

        if (patientAuthorizationService.isAdmin()) {
            return patientRepository.findAll();
        }

        if (!username.equals(patientAuthorizationService.currentUsername())) {
            return List.of();
        }

        return patientRepository.findByAssignedClinician(username);
    }

    public Optional<Patient> getPatient(Long id) {
        Optional<Patient> patient = patientRepository.findById(id);
        if (patient.isEmpty()) {
            return Optional.empty();
        }
        return patientAuthorizationService.enforceAccess(patient);
    }

    public Patient createPatient(Patient patient) {
        if (!patientAuthorizationService.isAdmin()) {
            throw new AccessDeniedException("Patient creation is restricted to ADMIN users.");
        }
        return patientRepository.save(patient);
    }

    public boolean canAccessPatient(Long id) {
        if (patientAuthorizationService.isAdmin()) {
            return true;
        }
        return patientAuthorizationService.canAccessPatient(id);
    }
}
