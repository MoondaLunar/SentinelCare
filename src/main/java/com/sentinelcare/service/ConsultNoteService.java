package com.sentinelcare.service;

import com.sentinelcare.entity.ConsultNote;
import com.sentinelcare.repository.ConsultNoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsultNoteService {

    private final ConsultNoteRepository consultNoteRepository;
    private final PatientAuthorizationService patientAuthorizationService;

    public ConsultNoteService(ConsultNoteRepository consultNoteRepository, PatientAuthorizationService patientAuthorizationService) {
        this.consultNoteRepository = consultNoteRepository;
        this.patientAuthorizationService = patientAuthorizationService;
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

    public ConsultNote createNote(ConsultNote consultNote) {
        if (consultNote == null || consultNote.getPatient() == null) {
            throw new IllegalArgumentException("Consult note requires a patient.");
        }
        if (!patientAuthorizationService.canAccessPatient(consultNote.getPatient())) {
            throw new SecurityException("Clinician cannot create notes for an unauthorized patient.");
        }
        return consultNoteRepository.save(consultNote);
    }
}
