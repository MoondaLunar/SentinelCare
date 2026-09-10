package com.sentinelcare.controller;

import com.sentinelcare.dto.ConsentCreateRequest;
import com.sentinelcare.entity.ConsentRecord;
import com.sentinelcare.service.ConsentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ConsentController {

    /**
     * Consent endpoints are central to privacy controls and future GDPR compliance workflows.
     */
    private final ConsentService consentService;

    public ConsentController(ConsentService consentService) {
        this.consentService = consentService;
    }

    @GetMapping("/consents")
    @PreAuthorize("hasAnyRole('ADMIN','CLINICIAN')")
    public List<ConsentRecord> getAllConsents() {
        return consentService.getAllConsents();
    }

    @GetMapping("/consents/active")
    @PreAuthorize("hasAnyRole('ADMIN','CLINICIAN')")
    public List<ConsentRecord> getActiveConsents() {
        return consentService.getActiveConsents();
    }

    @PostMapping("/consents")
    @PreAuthorize("hasAnyRole('ADMIN','CLINICIAN')")
    public ConsentRecord createConsent(@Valid @RequestBody ConsentCreateRequest request) {
        return consentService.createConsent(request);
    }

    @PostMapping("/consents/{id}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConsentRecord> revokeConsent(@PathVariable Long id) {
        return ResponseEntity.ok(consentService.revokeConsent(id));
    }
}