package com.sentinelcare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.envers.Audited;

import java.time.OffsetDateTime;

@Entity
@Table(name = "consent_records")
@Audited
public class ConsentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "consent_type", nullable = false)
    private String consentType;

    @Column(name = "granted", nullable = false)
    private boolean granted;

    @Column(name = "status", nullable = false)
    private ConsentStatus status = ConsentStatus.ACTIVE;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public ConsentRecord() {
    }

    public ConsentRecord(Patient patient, String consentType, boolean granted, String source) {
        this.patient = patient;
        this.consentType = consentType;
        this.granted = granted;
        this.source = source;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public String getConsentType() {
        return consentType;
    }

    public void setConsentType(String consentType) {
        this.consentType = consentType;
    }

    public boolean isGranted() {
        return granted;
    }

    public void setGranted(boolean granted) {
        this.granted = granted;
        if (!granted) {
            this.status = ConsentStatus.REVOKED;
        } else if (this.status == ConsentStatus.REVOKED || this.status == ConsentStatus.EXPIRED) {
            this.status = ConsentStatus.ACTIVE;
        }
    }

    public ConsentStatus getStatus() {
        return status;
    }

    public void setStatus(ConsentStatus status) {
        this.status = status;
        if (status == ConsentStatus.REVOKED) {
            this.granted = false;
        } else if (status == ConsentStatus.ACTIVE) {
            this.granted = true;
        }
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
