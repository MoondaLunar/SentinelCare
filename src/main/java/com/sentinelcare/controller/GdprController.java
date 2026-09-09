package com.sentinelcare.controller;

import com.sentinelcare.service.GdprService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class GdprController {

    /**
     * GDPR endpoints model the privacy-rights workflow for data deletion and patient access.
     * This is intentionally minimal today but maps directly to a real compliance process later.
     */
    private final GdprService gdprService;

    public GdprController(GdprService gdprService) {
        this.gdprService = gdprService;
    }

    @DeleteMapping("/gdpr/patients/{id}")
    public ResponseEntity<Map<String, String>> erasePatientData(@PathVariable Long id) {
        boolean erased = gdprService.erasePatientData(id);
        if (erased) {
            return ResponseEntity.ok(Map.of("status", "completed", "patientId", id.toString()));
        }
        return ResponseEntity.notFound().build();
    }
}
