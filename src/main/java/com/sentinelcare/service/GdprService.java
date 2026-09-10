package com.sentinelcare.service;

import com.sentinelcare.entity.ConsentRecord;
import com.sentinelcare.entity.ConsentStatus;
import com.sentinelcare.entity.Patient;
import com.sentinelcare.error.ConflictException;
import com.sentinelcare.error.ResourceNotFoundException;
import com.sentinelcare.repository.ConsentRecordRepository;
import com.sentinelcare.repository.ConsultNoteRepository;
import com.sentinelcare.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GdprService {

    private final PatientRepository patientRepository;
    private final ConsentRecordRepository consentRecordRepository;
    private final ConsultNoteRepository consultNoteRepository;
    private final PatientAuthorizationService patientAuthorizationService;
    private final AuditService auditService;

    public GdprService(PatientRepository patientRepository,
                       ConsentRecordRepository consentRecordRepository,
                       ConsultNoteRepository consultNoteRepository,
                       PatientAuthorizationService patientAuthorizationService,
                       AuditService auditService) {
        this.patientRepository = patientRepository;
        this.consentRecordRepository = consentRecordRepository;
        this.consultNoteRepository = consultNoteRepository;
        this.patientAuthorizationService = patientAuthorizationService;
        this.auditService = auditService;
    }

    /**
     * GDPR Art 17 erasure. A hard delete only happens when nothing depends on the
     * patient row. When consents or consult notes exist, deletion would break
     * retention duties (consents must never vanish; the clinical record is kept
     * under HIPAA 45 CFR 164.316(b)(2)(i)), so the request is refused with a
     * deliberate 409 naming the anonymize path instead of exploding into a 500
     * foreign-key error.
     */
    @Transactional
    public void erasePatientData(Long patientId) {
        if (!patientAuthorizationService.isAdmin()) {
            throw new SecurityException("Only ADMIN users may invoke patient erasure.");
        }

        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + patientId));

        boolean hasConsents = consentRecordRepository.existsByPatientId(patientId);
        boolean hasNotes = consultNoteRepository.existsByPatientId(patientId);
        if (hasConsents || hasNotes) {
            long consentCount = consentRecordRepository.countByPatientId(patientId);
            long noteCount = consultNoteRepository.countByPatientId(patientId);
            throw new ConflictException(
                "Patient record is retained: " + consentCount + " consent(s) and " + noteCount
                    + " consult note(s) exist. Hard deletion would break retention duties. "
                    + "Use POST /api/v1/gdpr/patients/" + patientId
                    + "/anonymize to erase identifiers while keeping the clinical record."
            );
        }

        auditService.record("PATIENT_ERASED", "Patient", patientId, "hard delete, no dependent records");
        patientRepository.delete(patient);
    }

    /**
     * GDPR Art 17(3)(b) anonymization path: identifiers are scrubbed, the
     * clinical record is kept, and every consent is revoked (consents never
     * vanish; their status machine already models that).
     */
    @Transactional
    public int anonymizePatientData(Long patientId) {
        if (!patientAuthorizationService.isAdmin()) {
            throw new SecurityException("Only ADMIN users may invoke patient anonymization.");
        }

        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + patientId));

        int revoked = 0;
        List<ConsentRecord> consents = consentRecordRepository.findByPatientId(patientId);
        for (ConsentRecord consent : consents) {
            if (consent.getStatus() != ConsentStatus.REVOKED) {
                consent.setStatus(ConsentStatus.REVOKED);
                revoked++;
            }
        }
        consentRecordRepository.saveAll(consents);

        patient.setName("ANONYMIZED-" + patientId);
        patient.setBirthdate(null);
        patient.setAssignedClinician(null);
        patientRepository.save(patient);

        auditService.record("PATIENT_ANONYMIZED", "Patient", patientId,
            "identifiers scrubbed; consents revoked=" + revoked);
        return revoked;
    }
}