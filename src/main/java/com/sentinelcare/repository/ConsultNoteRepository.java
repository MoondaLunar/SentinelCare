package com.sentinelcare.repository;

import com.sentinelcare.entity.ConsultNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsultNoteRepository extends JpaRepository<ConsultNote, Long> {
    java.util.List<ConsultNote> findByPatientAssignedClinician(String assignedClinician);
}
