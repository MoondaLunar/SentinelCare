package com.sentinelcare.controller;

import com.sentinelcare.entity.ConsultNote;
import com.sentinelcare.service.ConsultNoteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ConsultNoteController {

    /**
     * Clinical consult notes are intentionally exposed through a separate controller to support
     * specialist workflows and future patient-consent boundaries.
     */
    private final ConsultNoteService consultNoteService;

    public ConsultNoteController(ConsultNoteService consultNoteService) {
        this.consultNoteService = consultNoteService;
    }

    @GetMapping("/consult-notes")
    public List<ConsultNote> getAllNotes() {
        return consultNoteService.getAllNotes();
    }

    @PostMapping("/consult-notes")
    public ConsultNote createNote(@Valid @RequestBody ConsultNote consultNote) {
        return consultNoteService.createNote(consultNote);
    }
}
