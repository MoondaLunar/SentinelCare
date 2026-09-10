package com.sentinelcare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Core patient record.
 *
 * The entity is intentionally audited so treatment changes, consent updates, and sensitive
 * demographics can be reconstructed later for compliance and forensic review.
 */
@Entity
@Table(name = "patients")
@Audited
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    // Nullable in the schema so the anonymization path can scrub the date of birth;
    // @NotNull still enforces it as required input when patients are created.
    @NotNull
    @Column(name = "birthdate", nullable = true)
    private LocalDate birthdate;

    @NotBlank
    @Column(name = "diagnosis", nullable = false)
    private String diagnosis;

    @Column(name = "notes", length = 2048)
    private String notes;

    @Column(name = "assigned_clinician")
    private String assignedClinician;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Patient() {
    }

    public Patient(String name, LocalDate birthdate, String diagnosis, String notes) {
        this.name = name;
        this.birthdate = birthdate;
        this.diagnosis = diagnosis;
        this.notes = notes;
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        touchUpdatedAt();
    }

    private void touchUpdatedAt() {
        OffsetDateTime now = OffsetDateTime.now();
        if (this.updatedAt != null && !now.isAfter(this.updatedAt)) {
            this.updatedAt = this.updatedAt.plusNanos(1);
            return;
        }
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        touchUpdatedAt();
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
        touchUpdatedAt();
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
        touchUpdatedAt();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
        touchUpdatedAt();
    }

    public String getAssignedClinician() {
        return assignedClinician;
    }

    public void setAssignedClinician(String assignedClinician) {
        this.assignedClinician = assignedClinician;
        touchUpdatedAt();
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
