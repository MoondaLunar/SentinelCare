package com.sentinelcare.controller;

import com.sentinelcare.entity.AuditEntry;
import com.sentinelcare.repository.AuditEntryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AuditController {

    private final AuditEntryRepository auditEntryRepository;

    public AuditController(AuditEntryRepository auditEntryRepository) {
        this.auditEntryRepository = auditEntryRepository;
    }

    @GetMapping("/audit")
    public List<AuditEntry> getAuditEntries() {
        return auditEntryRepository.findAll();
    }
}
