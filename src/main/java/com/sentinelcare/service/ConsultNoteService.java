package com.sentinelcare.service;

import com.sentinelcare.dto.ConsultNoteCreateRequest;
import com.sentinelcare.entity.ConsultNote;
import com.sentinelcare.entity.Patient;
import com.sentinelcare.error.ResourceNotFoundException;
import com.sentinelcare.repository.ConsultNoteRepository;
import com.sentinelcare.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConsultNoteService {

    private final ConsultNoteRepository consultNoteRepository;
    private final PatientRepository patientRepository;
    private final PatientAuthorizationService patientAuthorizationService;
    private final AuditService auditService;

    public ConsultNoteService(ConsultNoteRepository consultNoteRepository,
                              PatientRepository patientRepository,
                              PatientAuthorizationService patientAuthorizationService,
                              AuditService auditService) {
        this.consultNoteRepository = consultNoteRepository;
        this.patientRepository = patientRepository;
        this.patientAuthorizationService = patientAuthorizationService;
        this.auditService = auditService;
    }

    public List<ConsultNote> getAllNotes() {
        if (patientAuthorizationService.isAdmin()) {
            return consultNoteRepository.findAll();
        }
        String username = patientAuthorizationService.currentUsername();
        if (username == null || username.isBlank()) {
            return List.of();
        }
        return consultNoteRepository.findByPatientAssignedClinician(username);
    }

    @Transactional
    public ConsultNote createNote(ConsultNoteCreateRequest request) {
        if (request == null || request.getPatientId() == null) {
            throw new IllegalArgumentException("Consult note requires a patientId.");
        }

        Patient patient = patientRepository.findById(request.getPatientId())
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + request.getPatientId()));

        if (!patientAuthorizationService.canAccessPatient(patient)) {
            auditService.recordDenied("NOTE_CREATE_DENIED", "Patient", patient.getId(),
                "consult note creation attempted on unauthorized patient");
            throw new SecurityException("Clinician cannot create notes for an unauthorized patient.");
        }

        ConsultNote consultNote = new ConsultNote(patient, request.getProviderName(), request.getNoteText());
        ConsultNote saved = consultNoteRepository.save(consultNote);
        auditService.record("NOTE_CREATED", "ConsultNote", saved.getId(),
            "patient=" + patient.getId());
        return saved;
    }
}