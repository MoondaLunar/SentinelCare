package com.sentinelcare.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PatientTest {

    @Test
    void constructorShouldPopulateFields() {
        LocalDate birthdate = LocalDate.of(1990, 2, 15);

        Patient patient = new Patient("Jane Doe", birthdate, "Hypertension", "Follow-up in 2 weeks");

        assertNull(patient.getId());
        assertEquals("Jane Doe", patient.getName());
        assertEquals(birthdate, patient.getBirthdate());
        assertEquals("Hypertension", patient.getDiagnosis());
        assertEquals("Follow-up in 2 weeks", patient.getNotes());
        assertNotNull(patient.getCreatedAt());
        assertNotNull(patient.getUpdatedAt());
    }

    @Test
    void settersShouldUpdateUpdatedAt() {
        Patient patient = new Patient("John Doe", LocalDate.of(1985, 5, 1), "Flu", "Initial note");
        OffsetDateTime originalUpdatedAt = patient.getUpdatedAt();

        patient.setName("John Smith");
        patient.setBirthdate(LocalDate.of(1986, 5, 1));
        patient.setDiagnosis("Migraine");
        patient.setNotes("Updated note");

        assertEquals("John Smith", patient.getName());
        assertEquals(LocalDate.of(1986, 5, 1), patient.getBirthdate());
        assertEquals("Migraine", patient.getDiagnosis());
        assertEquals("Updated note", patient.getNotes());
        assertTrue(patient.getUpdatedAt().isAfter(originalUpdatedAt));
    }
}
