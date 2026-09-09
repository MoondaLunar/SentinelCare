package com.sentinelcare.controller;

import com.sentinelcare.entity.ConsentRecord;
import com.sentinelcare.service.ConsentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
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
    public List<ConsentRecord> getAllConsents() {
        return consentService.getAllConsents();
    }

    @PostMapping("/consents")
    public ConsentRecord createConsent(@Valid @RequestBody ConsentRecord consentRecord) {
        return consentService.createConsent(consentRecord);
    }
}
