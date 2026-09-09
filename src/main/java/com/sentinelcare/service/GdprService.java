package com.sentinelcare.service;

import com.sentinelcare.entity.Patient;
import com.sentinelcare.repository.PatientRepository;
import org.springframework.stereotype.Service;

@Service
public class GdprService {

    /**
     * GDPR orchestration is intentionally separate from persistence so future compliance behavior,
     * like secure deletion, logging, and hold states, remains easy to evolve without coupling to
     * the controller layer.
     */
    private final PatientRepository patientRepository;

    public GdprService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public boolean erasePatientData(Long patientId) {
        Patient patient = patientRepository.findById(patientId).orElse(null);
        if (patient == null) {
            return false;
        }

        patientRepository.delete(patient);
        return true;
    }
}
