CREATE TABLE consent_records (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    consent_type VARCHAR(255) NOT NULL,
    granted BOOLEAN NOT NULL,
    source VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_consent_patient FOREIGN KEY (patient_id) REFERENCES patients(id)
);

CREATE TABLE consult_notes (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    provider_name VARCHAR(255) NOT NULL,
    note_text VARCHAR(4096) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_consult_patient FOREIGN KEY (patient_id) REFERENCES patients(id)
);
