package com.sentinelcare.service;

import com.sentinelcare.entity.ConsentRecord;
import com.sentinelcare.entity.ConsentStatus;
import com.sentinelcare.repository.ConsentRecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConsentService {

    private final ConsentRecordRepository consentRecordRepository;
    private final PatientAuthorizationService patientAuthorizationService;

    public ConsentService(ConsentRecordRepository consentRecordRepository, PatientAuthorizationService patientAuthorizationService) {
        this.consentRecordRepository = consentRecordRepository;
        this.patientAuthorizationService = patientAuthorizationService;
    }

    public List<ConsentRecord> getAllConsents() {
        if (patientAuthorizationService.isAdmin()) {
            return consentRecordRepository.findAll();
        }
        String username = patientAuthorizationService.currentUsername();
        if (username == null || username.isBlank()) {
            return List.of();
        }
        return consentRecordRepository.findByPatientAssignedClinician(username);
    }

    public List<ConsentRecord> getActiveConsents() {
        return getAllConsents().stream()
            .filter(record -> record.getStatus() == ConsentStatus.ACTIVE && record.isGranted())
            .toList();
    }

    public ConsentRecord createConsent(ConsentRecord consentRecord) {
        if (consentRecord == null || consentRecord.getPatient() == null) {
            throw new IllegalArgumentException("Consent record requires a patient.");
        }
        if (!patientAuthorizationService.canAccessPatient(consentRecord.getPatient())) {
            throw new SecurityException("Clinician cannot operate on unauthorized patient consent.");
        }
        if (consentRecord.getStatus() == null) {
            consentRecord.setStatus(consentRecord.isGranted() ? ConsentStatus.ACTIVE : ConsentStatus.REVOKED);
        }
        return consentRecordRepository.save(consentRecord);
    }

    public ConsentRecord revokeConsent(Long id) {
        ConsentRecord consentRecord = consentRecordRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Consent record not found: " + id));
        if (!patientAuthorizationService.isAdmin() && !patientAuthorizationService.canAccessPatient(consentRecord.getPatient())) {
            throw new SecurityException("Clinician cannot revoke unauthorized consent.");
        }
        consentRecord.setStatus(ConsentStatus.REVOKED);
        return consentRecordRepository.save(consentRecord);
    }

    public Optional<ConsentRecord> findById(Long id) {
        Optional<ConsentRecord> consentRecord = consentRecordRepository.findById(id);
        if (consentRecord.isEmpty()) {
            return Optional.empty();
        }
        if (patientAuthorizationService.isAdmin()) {
            return consentRecord;
        }
        return patientAuthorizationService.canAccessPatient(consentRecord.get().getPatient()) ? consentRecord : Optional.empty();
    }
}
