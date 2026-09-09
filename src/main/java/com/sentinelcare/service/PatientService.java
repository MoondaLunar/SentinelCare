package com.sentinelcare.service;

import com.sentinelcare.entity.Patient;
import com.sentinelcare.repository.PatientRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatientService {

    /**
     * Repository-backed access point for patient records.
     *
     * This service intentionally isolates persistence concerns from the controller layer,
     * making it easier to add validation, audit logging, and access policies later.
     */
    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    /**
     * Returns the current patient registry for presentation or downstream workflows.
     */
    public List<Patient> getAllPatients() {
        return getVisiblePatients(currentUsername());
    }

    /**
     * Returns only the records a clinician is allowed to see. Admin users can view all patients,
     * while clinicians are restricted to the patient set assigned to them.
     */
    public List<Patient> getVisiblePatients(String username) {
        if (username == null || username.isBlank()) {
            return patientRepository.findAll();
        }

        if (isAdmin()) {
            return patientRepository.findAll();
        }

        return patientRepository.findAll().stream()
            .filter(patient -> username.equals(patient.getAssignedClinician()))
            .collect(Collectors.toList());
    }

    /**
     * Retrieves a patient by identifier after enforcing the current caller's access rights.
     */
    public Optional<Patient> getPatient(Long id) {
        Optional<Patient> patient = patientRepository.findById(id);
        if (patient.isEmpty()) {
            return Optional.empty();
        }

        if (isAdmin()) {
            return patient;
        }

        String username = currentUsername();
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }

        return username.equals(patient.get().getAssignedClinician()) ? patient : Optional.empty();
    }

    /**
     * Persists a newly created patient and automatically assigns it to the acting clinician.
     * Admin-created records remain unassigned unless a clinician explicitly sets the owner.
     */
    public Patient createPatient(Patient patient) {
        if (!isAdmin()) {
            String username = currentUsername();
            if (username != null && !username.isBlank()) {
                patient.setAssignedClinician(username);
            }
        }
        return patientRepository.save(patient);
    }

    /**
     * Centralizes the access decision for patient-scoped authorization.
     */
    public boolean canAccessPatient(Long id) {
        if (isAdmin()) {
            return true;
        }

        String username = currentUsername();
        if (username == null || username.isBlank()) {
            return false;
        }

        return patientRepository.findById(id)
            .map(patient -> username.equals(patient.getAssignedClinician()))
            .orElse(false);
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ADMIN"));
    }
}
