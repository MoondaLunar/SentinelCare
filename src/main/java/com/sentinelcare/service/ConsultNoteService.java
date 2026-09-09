package com.sentinelcare.service;

import com.sentinelcare.entity.ConsultNote;
import com.sentinelcare.repository.ConsultNoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsultNoteService {

    /**
     * Consult notes represent privacy-boundary clinical communication. They are separated from the
     * patient profile to support safe sharing and later retrieval across specialist workflows.
     */
    private final ConsultNoteRepository consultNoteRepository;

    public ConsultNoteService(ConsultNoteRepository consultNoteRepository) {
        this.consultNoteRepository = consultNoteRepository;
    }

    public List<ConsultNote> getAllNotes() {
        return consultNoteRepository.findAll();
    }

    public ConsultNote createNote(ConsultNote consultNote) {
        return consultNoteRepository.save(consultNote);
    }
}
