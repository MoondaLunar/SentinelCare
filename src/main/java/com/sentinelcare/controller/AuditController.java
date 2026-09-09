package com.sentinelcare.controller;

import com.sentinelcare.entity.AuditEntry;
import com.sentinelcare.service.AuditService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AuditController {

    /**
     * Audit history is essential for healthcare compliance, forensics, and trust auditing.
     */
    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/audit")
    public List<AuditEntry> getAuditEntries() {
        return auditService.getAllEntries();
    }

    @GetMapping("/audit/{entityType}/{entityId}")
    public List<AuditEntry> getEntityAuditEntries(@PathVariable String entityType, @PathVariable Long entityId) {
        return auditService.getEntriesForEntity(entityType, entityId);
    }
}
