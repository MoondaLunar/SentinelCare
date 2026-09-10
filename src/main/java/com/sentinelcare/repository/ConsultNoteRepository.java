package com.sentinelcare.repository;

import com.sentinelcare.entity.ConsultNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultNoteRepository extends JpaRepository<ConsultNote, Long> {
    List<ConsultNote> findByPatientAssignedClinician(String assignedClinician);

    boolean existsByPatientId(Long patientId);

    long countByPatientId(Long patientId);
}