package com.sentinelcare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request body for consult-note creation. Only a patientId references the
 * patient; the server reloads the row and authorizes against it.
 */
public class ConsultNoteCreateRequest {

    @NotNull(message = "patientId is required")
    private Long patientId;

    @NotBlank(message = "providerName is required")
    @Size(max = 255, message = "providerName must be at most 255 characters")
    private String providerName;

    @NotBlank(message = "noteText is required")
    @Size(max = 4096, message = "noteText must be at most 4096 characters")
    private String noteText;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }
}