package com.sentinelcare.service;

import com.sentinelcare.entity.AuditEntry;
import com.sentinelcare.entity.ConsentRecord;
import com.sentinelcare.entity.ConsentStatus;
import com.sentinelcare.entity.Patient;
import com.sentinelcare.error.ConflictException;
import com.sentinelcare.repository.AuditEntryRepository;
import com.sentinelcare.repository.ConsentRecordRepository;
import com.sentinelcare.repository.PatientRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Erasure paths against a real JPA stack (H2, create-drop).
 *
 * The controller-level tests mock the service, so they never exercised
 * Hibernate's update-time bean validation. These tests run the real
 * transaction and catch update-path regressions such as a create-only
 * constraint blocking anonymization.
 */
@SpringBootTest
@ActiveProfiles("test")
class GdprErasureIntegrationTest {

    @Autowired
    private GdprService gdprService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ConsentRecordRepository consentRecordRepository;

    @Autowired
    private AuditEntryRepository auditEntryRepository;

    @BeforeEach
    void authenticateAsAdmin() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("admin", null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void anonymizeShouldScrubIdentifiersAndRevokeConsents() {
        Patient patient = patientRepository.save(
            new Patient("Erasure Target", LocalDate.of(1980, 1, 1), "Dx", "note"));
        patient.setAssignedClinician("clinician");
        patient = patientRepository.save(patient);
        Long patientId = patient.getId();

        ConsentRecord consent = consentRecordRepository.save(
            new ConsentRecord(patient, "TREATMENT", true, "integration test"));

        int revoked = gdprService.anonymizePatientData(patientId);

        assertEquals(1, revoked);
        Patient reloaded = patientRepository.findById(patientId).orElseThrow();
        assertEquals("ANONYMIZED-" + patientId, reloaded.getName());
        assertNull(reloaded.getBirthdate());
        assertNull(reloaded.getAssignedClinician());
        assertEquals(ConsentStatus.REVOKED,
            consentRecordRepository.findById(consent.getId()).orElseThrow().getStatus());

        List<AuditEntry> entries = auditEntryRepository.findByEntityTypeAndEntityId("Patient", patientId);
        assertTrue(entries.stream().anyMatch(e ->
            "PATIENT_ANONYMIZED".equals(e.getAction()) && "admin".equals(e.getActor())));
    }

    @Test
    void eraseWithoutDependentsShouldHardDelete() {
        Patient patient = patientRepository.save(
            new Patient("Delete Me", LocalDate.of(1990, 2, 2), "Dx", null));

        gdprService.erasePatientData(patient.getId());

        assertTrue(patientRepository.findById(patient.getId()).isEmpty());
    }

    @Test
    void eraseWithDependentsShouldConflict() {
        Patient patient = patientRepository.save(
            new Patient("Retain Me", LocalDate.of(1990, 3, 3), "Dx", null));
        consentRecordRepository.save(new ConsentRecord(patient, "TREATMENT", true, "integration test"));

        assertThrows(ConflictException.class, () -> gdprService.erasePatientData(patient.getId()));
        assertTrue(patientRepository.findById(patient.getId()).isPresent());
    }
}
