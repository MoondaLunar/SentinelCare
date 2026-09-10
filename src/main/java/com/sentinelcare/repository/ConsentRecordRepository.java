package com.sentinelcare.repository;

import com.sentinelcare.entity.ConsentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsentRecordRepository extends JpaRepository<ConsentRecord, Long> {
    List<ConsentRecord> findByPatientAssignedClinician(String assignedClinician);

    List<ConsentRecord> findByPatientId(Long patientId);

    boolean existsByPatientId(Long patientId);

    long countByPatientId(Long patientId);
}