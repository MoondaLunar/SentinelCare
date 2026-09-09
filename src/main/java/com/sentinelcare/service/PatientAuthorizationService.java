package com.sentinelcare.service;

import com.sentinelcare.entity.Patient;
import com.sentinelcare.repository.PatientRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientAuthorizationService {

    private final PatientRepository patientRepository;

    public PatientAuthorizationService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ADMIN"));
    }

    public String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String username = authentication.getName();
        return (username == null || username.isBlank()) ? null : username;
    }

    public boolean isClinician() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role -> role.equals("ROLE_CLINICIAN") || role.equals("CLINICIAN"));
    }

    public boolean canAccessPatient(Patient patient) {
        if (patient == null) {
            return false;
        }
        if (isAdmin()) {
            return true;
        }
        String username = currentUsername();
        if (username == null || username.isBlank()) {
            return false;
        }
        return username.equals(patient.getAssignedClinician());
    }

    public boolean canAccessPatient(Long patientId) {
        if (isAdmin()) {
            return true;
        }
        String username = currentUsername();
        if (username == null || username.isBlank()) {
            return false;
        }
        return patientRepository.findById(patientId)
            .map(patient -> username.equals(patient.getAssignedClinician()))
            .orElse(false);
    }

    public List<Patient> filterVisiblePatients(List<Patient> patients) {
        if (patients == null) {
            return List.of();
        }
        if (isAdmin()) {
            return patients;
        }
        String username = currentUsername();
        if (username == null || username.isBlank()) {
            return List.of();
        }
        return patients.stream()
            .filter(this::canAccessPatient)
            .toList();
    }

    public Optional<Patient> enforceAccess(Optional<Patient> patient) {
        if (patient.isEmpty()) {
            return Optional.empty();
        }
        return canAccessPatient(patient.get()) ? patient : Optional.empty();
    }
}
