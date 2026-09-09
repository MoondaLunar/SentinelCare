package com.sentinelcare.service;

import com.sentinelcare.entity.AuditEntry;
import com.sentinelcare.entity.Patient;
import com.sentinelcare.repository.AuditEntryRepository;
import com.sentinelcare.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    private final AuditEntryRepository auditEntryRepository;
    private final PatientAuthorizationService patientAuthorizationService;
    private final PatientRepository patientRepository;

    public AuditService(AuditEntryRepository auditEntryRepository,
                       PatientAuthorizationService patientAuthorizationService,
                       PatientRepository patientRepository) {
        this.auditEntryRepository = auditEntryRepository;
        this.patientAuthorizationService = patientAuthorizationService;
        this.patientRepository = patientRepository;
    }

    public List<AuditEntry> getAllEntries() {
        if (patientAuthorizationService.isAdmin()) {
            return auditEntryRepository.findAll();
        }
        String username = patientAuthorizationService.currentUsername();
        if (username == null || username.isBlank()) {
            return List.of();
        }

        List<Patient> accessiblePatients = patientRepository.findByAssignedClinician(username);
        if (accessiblePatients.isEmpty()) {
            return List.of();
        }

        return auditEntryRepository.findAll().stream()
            .filter(this::isAccessibleAuditEntry)
            .toList();
    }

    public List<AuditEntry> getEntriesForEntity(String entityType, Long entityId) {
        if (patientAuthorizationService.isAdmin()) {
            return auditEntryRepository.findByEntityTypeAndEntityId(entityType, entityId);
        }
        String username = patientAuthorizationService.currentUsername();
        if (username == null || username.isBlank()) {
            return List.of();
        }

        if (!patientAuthorizationService.canAccessPatient(entityId)) {
            return List.of();
        }

        return auditEntryRepository.findByEntityTypeAndEntityId(entityType, entityId).stream()
            .filter(this::isAccessibleAuditEntry)
            .toList();
    }

    public AuditEntry createEntry(AuditEntry auditEntry) {
        return auditEntryRepository.save(auditEntry);
    }

    private boolean isAccessibleAuditEntry(AuditEntry entry) {
        if (entry == null || entry.getEntityType() == null) {
            return false;
        }
        if (!"Patient".equals(entry.getEntityType())) {
            return false;
        }
        Patient patient = patientRepository.findById(entry.getEntityId()).orElse(null);
        return patientAuthorizationService.canAccessPatient(patient);
    }
}
