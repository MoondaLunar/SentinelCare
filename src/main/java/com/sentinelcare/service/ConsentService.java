package com.sentinelcare.service;

import com.sentinelcare.dto.ConsentCreateRequest;
import com.sentinelcare.entity.ConsentRecord;
import com.sentinelcare.entity.ConsentStatus;
import com.sentinelcare.entity.Patient;
import com.sentinelcare.error.ResourceNotFoundException;
import com.sentinelcare.repository.ConsentRecordRepository;
import com.sentinelcare.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ConsentService {

    private final ConsentRecordRepository consentRecordRepository;
    private final PatientRepository patientRepository;
    private final PatientAuthorizationService patientAuthorizationService;
    private final AuditService auditService;

    public ConsentService(ConsentRecordRepository consentRecordRepository,
                          PatientRepository patientRepository,
                          PatientAuthorizationService patientAuthorizationService,
                          AuditService auditService) {
        this.consentRecordRepository = consentRecordRepository;
        this.patientRepository = patientRepository;
        this.patientAuthorizationService = patientAuthorizationService;
        this.auditService = auditService;
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

    @Transactional
    public ConsentRecord createConsent(ConsentCreateRequest request) {
        if (request == null || request.getPatientId() == null) {
            throw new IllegalArgumentException("Consent request requires a patientId.");
        }
        if (request.getGranted() == null) {
            throw new IllegalArgumentException("Consent request requires the granted flag.");
        }

        Patient patient = patientRepository.findById(request.getPatientId())
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + request.getPatientId()));

        if (!patientAuthorizationService.canAccessPatient(patient)) {
            auditService.recordDenied("CONSENT_CREATE_DENIED", "Patient", request.getPatientId(),
                "consent creation attempted on unauthorized patient");
            throw new SecurityException("Clinician cannot operate on unauthorized patient consent.");
        }

        ConsentRecord consentRecord = new ConsentRecord(
            patient, request.getConsentType(), request.getGranted(), request.getSource());
        consentRecord.setStatus(request.getGranted() ? ConsentStatus.ACTIVE : ConsentStatus.REVOKED);

        ConsentRecord saved = consentRecordRepository.save(consentRecord);
        auditService.record("CONSENT_CREATED", "ConsentRecord", saved.getId(),
            "type=" + saved.getConsentType() + ", patient=" + patient.getId());
        return saved;
    }

    @Transactional
    public ConsentRecord revokeConsent(Long id) {
        ConsentRecord consentRecord = consentRecordRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Consent record not found: " + id));

        if (!patientAuthorizationService.isAdmin()
                && !patientAuthorizationService.canAccessPatient(consentRecord.getPatient())) {
            auditService.recordDenied("CONSENT_REVOKE_DENIED", "ConsentRecord", id,
                "consent revocation attempted on unauthorized consent");
            throw new SecurityException("Clinician cannot revoke unauthorized consent.");
        }

        consentRecord.setStatus(ConsentStatus.REVOKED);
        ConsentRecord saved = consentRecordRepository.save(consentRecord);
        auditService.record("CONSENT_REVOKED", "ConsentRecord", saved.getId(),
            "type=" + saved.getConsentType());
        return saved;
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