package com.sentinelcare.security;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable, in-memory lifecycle snapshot for a restricted emergency session.
 * No authentication, token issuance, persistence, or controller integration.
 */
public record EmergencySession(
    UUID id,
    UUID requesterId,
    UUID patientId,
    Instant createdAt,
    Instant expiresAt,
    Status status
) {
    public enum Status { PENDING_APPROVAL, APPROVED, DENIED, REVOKED, EXPIRED }

    public EmergencySession {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(requesterId, "requesterId");
        Objects.requireNonNull(patientId, "patientId");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
        Objects.requireNonNull(status, "status");
        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("expiry must follow creation");
        }
    }

    public static EmergencySession pending(UUID id, UUID requesterId, UUID patientId,
                                           Instant createdAt, Instant expiresAt) {
        return new EmergencySession(id, requesterId, patientId, createdAt, expiresAt,
                                    Status.PENDING_APPROVAL);
    }

    /** A pending session permits only separately authorized emergency-summary reads
     * and, for explicitly permitted critical-care staff, append-only urgent notes.
     */
    public boolean isActiveAt(Instant now) {
        Objects.requireNonNull(now, "now");
        return now.isBefore(expiresAt)
            && !now.isBefore(createdAt)
            && (status == Status.PENDING_APPROVAL || status == Status.APPROVED);
    }

    public boolean permitsPatient(UUID requestedPatientId, Instant now) {
        return patientId.equals(requestedPatientId) && isActiveAt(now);
    }

    public EmergencySession approve(Instant now) {
        requirePendingAndActive(now);
        return withStatus(Status.APPROVED);
    }

    public EmergencySession deny(Instant now) {
        requirePendingAndActive(now);
        return withStatus(Status.DENIED);
    }

    public EmergencySession revoke(Instant now) {
        Objects.requireNonNull(now, "now");
        if (!isActiveAt(now)) {
            throw new IllegalStateException("only active sessions can be revoked");
        }
        return withStatus(Status.REVOKED);
    }

    public EmergencySession expire(Instant now) {
        Objects.requireNonNull(now, "now");
        if (now.isBefore(expiresAt)) {
            throw new IllegalStateException("session has not reached expiry");
        }
        if (status == Status.DENIED || status == Status.REVOKED || status == Status.EXPIRED) {
            return this;
        }
        return withStatus(Status.EXPIRED);
    }

    private void requirePendingAndActive(Instant now) {
        if (status != Status.PENDING_APPROVAL || !isActiveAt(now)) {
            throw new IllegalStateException("transition requires an active pending session");
        }
    }

    private EmergencySession withStatus(Status next) {
        return new EmergencySession(id, requesterId, patientId, createdAt, expiresAt, next);
    }
}
