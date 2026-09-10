package com.sentinelcare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request body for consent creation. The client supplies a patientId plus the
 * consent facts; the server reloads the patient row and derives both
 * authorization and status, so neither can be spoofed from the request body.
 */
public class ConsentCreateRequest {

    @NotNull(message = "patientId is required")
    private Long patientId;

    @NotBlank(message = "consentType is required")
    @Size(max = 255, message = "consentType must be at most 255 characters")
    private String consentType;

    @NotNull(message = "granted is required")
    private Boolean granted;

    @NotBlank(message = "source is required")
    @Size(max = 255, message = "source must be at most 255 characters")
    private String source;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getConsentType() {
        return consentType;
    }

    public void setConsentType(String consentType) {
        this.consentType = consentType;
    }

    public Boolean getGranted() {
        return granted;
    }

    public void setGranted(Boolean granted) {
        this.granted = granted;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}