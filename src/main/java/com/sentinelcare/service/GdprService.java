package com.sentinelcare.service;

import com.sentinelcare.entity.Patient;
import com.sentinelcare.repository.PatientRepository;
import org.springframework.stereotype.Service;

@Service
public class GdprService {

    private final PatientRepository patientRepository;
    private final PatientAuthorizationService patientAuthorizationService;

    public GdprService(PatientRepository patientRepository, PatientAuthorizationService patientAuthorizationService) {
        this.patientRepository = patientRepository;
        this.patientAuthorizationService = patientAuthorizationService;
    }

    public boolean erasePatientData(Long patientId) {
        if (!patientAuthorizationService.isAdmin()) {
            throw new SecurityException("Only ADMIN users may invoke patient erasure.");
        }

        Patient patient = patientRepository.findById(patientId).orElse(null);
        if (patient == null) {
            return false;
        }

        // This is intentionally conservative: the service models a controlled administrative request,
        // not a complete production-ready legal erasure implementation.
        patientRepository.delete(patient);
        return true;
    }
}
