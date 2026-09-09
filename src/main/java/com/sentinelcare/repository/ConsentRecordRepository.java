package com.sentinelcare.repository;

import com.sentinelcare.entity.ConsentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsentRecordRepository extends JpaRepository<ConsentRecord, Long> {
    java.util.List<ConsentRecord> findByPatientAssignedClinician(String assignedClinician);
}
