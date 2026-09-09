package com.sentinelcare.service;

import com.sentinelcare.entity.Patient;
import com.sentinelcare.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    /**
     * Repository-backed access point for patient records.
     *
     * This service intentionally isolates persistence concerns from the controller layer,
     * making it easier to add validation, audit logging, and access policies later.
     */
    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    /**
     * Returns the current patient registry for presentation or downstream workflows.
     */
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    /**
     * Retrieves a patient by identifier.
     *
     * This is kept intentionally simple for now; future iterations can enforce role-based
     * access rules or trigger audit events before returning the record.
     */
    public Optional<Patient> getPatient(Long id) {
        return patientRepository.findById(id);
    }

    /**
     * Persists a newly created patient record.
     *
     * Future work should add consent checks, validation rules, and audit notifications here
     * before the entity is saved.
     */
    public Patient createPatient(Patient patient) {
        return patientRepository.save(patient);
    }
}
