package com.sentinelcare.controller;

import com.sentinelcare.service.GdprService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class GdprController {

    /**
     * GDPR endpoints model the privacy-rights workflow for data deletion and patient access.
     * Erasure has two deliberate outcomes: a hard delete when nothing depends on the
     * record, or an explicit retention conflict whose answer is anonymization.
     */
    private final GdprService gdprService;

    public GdprController(GdprService gdprService) {
        this.gdprService = gdprService;
    }

    @DeleteMapping("/gdpr/patients/{id}")
    public ResponseEntity<Map<String, String>> erasePatientData(@PathVariable Long id) {
        gdprService.erasePatientData(id);
        return ResponseEntity.ok(Map.of("status", "deleted", "patientId", id.toString()));
    }

    @PostMapping("/gdpr/patients/{id}/anonymize")
    public ResponseEntity<Map<String, Object>> anonymizePatientData(@PathVariable Long id) {
        int revokedConsents = gdprService.anonymizePatientData(id);
        return ResponseEntity.ok(Map.of(
            "status", "anonymized",
            "patientId", id.toString(),
            "revokedConsents", revokedConsents
        ));
    }
}