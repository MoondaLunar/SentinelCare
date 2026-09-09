package com.sentinelcare.service;

import com.sentinelcare.entity.ConsentRecord;
import com.sentinelcare.repository.ConsentRecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public ConsentRecord createConsent(ConsentRecord consentRecord) {
        return consentRecordRepository.save(consentRecord);
    }
}
