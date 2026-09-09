package com.sentinelcare.service;

import com.sentinelcare.entity.ConsentRecord;
import com.sentinelcare.entity.ConsentStatus;
import com.sentinelcare.repository.ConsentRecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConsentService {

    /**
     * Consent records are a key compliance control; they capture which patient permissions were
     * granted or revoked and by what source. This service keeps that record-keeping logic easy to
     * extend for consent expiry checks and audit trail generation.
     */
    private final ConsentRecordRepository consentRecordRepository;

    public ConsentService(ConsentRecordRepository consentRecordRepository) {
        this.consentRecordRepository = consentRecordRepository;
    }

    public List<ConsentRecord> getAllConsents() {
        return consentRecordRepository.findAll();
    }

    public List<ConsentRecord> getActiveConsents() {
        return consentRecordRepository.findAll().stream()
            .filter(record -> record.getStatus() == ConsentStatus.ACTIVE && record.isGranted())
            .toList();
    }

    public ConsentRecord createConsent(ConsentRecord consentRecord) {
        if (consentRecord.getStatus() == null) {
            consentRecord.setStatus(consentRecord.isGranted() ? ConsentStatus.ACTIVE : ConsentStatus.REVOKED);
        }
        return consentRecordRepository.save(consentRecord);
    }

    public ConsentRecord revokeConsent(Long id) {
        ConsentRecord consentRecord = consentRecordRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Consent record not found: " + id));

        consentRecord.setStatus(ConsentStatus.REVOKED);
        return consentRecordRepository.save(consentRecord);
    }

    public Optional<ConsentRecord> findById(Long id) {
        return consentRecordRepository.findById(id);
    }
}
