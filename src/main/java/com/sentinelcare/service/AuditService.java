package com.sentinelcare.service;

import com.sentinelcare.entity.AuditEntry;
import com.sentinelcare.repository.AuditEntryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    /**
     * Audit entries are the compliance backbone for privacy-sensitive healthcare workflows.
     * This service keeps access to the audit log isolated so future filtering and retention logic
     * is easy to add without leaking repository details into the API layer.
     */
    private final AuditEntryRepository auditEntryRepository;

    public AuditService(AuditEntryRepository auditEntryRepository) {
        this.auditEntryRepository = auditEntryRepository;
    }

    public List<AuditEntry> getAllEntries() {
        return auditEntryRepository.findAll();
    }

    public List<AuditEntry> getEntriesForEntity(String entityType, Long entityId) {
        return auditEntryRepository.findAll().stream()
            .filter(entry -> entityType.equals(entry.getEntityType()) && entityId.equals(entry.getEntityId()))
            .toList();
    }

    public AuditEntry createEntry(AuditEntry auditEntry) {
        return auditEntryRepository.save(auditEntry);
    }
}
