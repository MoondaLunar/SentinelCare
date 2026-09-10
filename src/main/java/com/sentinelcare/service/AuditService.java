package com.sentinelcare.service;

import com.sentinelcare.entity.AuditEntry;
import com.sentinelcare.entity.Patient;
import com.sentinelcare.repository.AuditEntryRepository;
import com.sentinelcare.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Single choke point for the audit write path: every event record goes
     * through this method, so nothing writes audit_entries on its own.
     */
    @Transactional
    public AuditEntry record(String action, String entityType, Long entityId, String details) {
        String actor = patientAuthorizationService.currentUsername();
        if (actor == null || actor.isBlank()) {
            actor = "system";
        }
        return createEntry(new AuditEntry(actor, action, entityType, entityId, details));
    }

    /**
     * Security-event records must survive the rollback of the operation they
     * describe, so they run in a transaction of their own.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditEntry recordDenied(String action, String entityType, Long entityId, String details) {
        return record(action, entityType, entityId, details);
    }

    private AuditEntry createEntry(AuditEntry auditEntry) {
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