-- V4: align schema with entities (fresh Postgres + ddl-auto: validate was failing on boot)
-- patients.assigned_clinician exists in Patient.java but was never in a migration (V1)
ALTER TABLE patients ADD COLUMN assigned_clinician VARCHAR(255);

-- consent_records.status exists in ConsentRecord.java (ConsentStatus enum, ordinal mapping, NOT NULL)
-- but was never in a migration (V3). Hibernate 6 maps an ordinal enum to SMALLINT.
ALTER TABLE consent_records ADD COLUMN status SMALLINT NOT NULL DEFAULT 0;

-- Backfill pre-status rows to match their semantics: granted -> ACTIVE(0), revoked -> REVOKED(1)
UPDATE consent_records SET status = CASE WHEN granted THEN 0 ELSE 1 END;
