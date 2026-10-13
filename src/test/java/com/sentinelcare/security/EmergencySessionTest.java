package com.sentinelcare.security;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmergencySessionTest {
    private static final Instant START = Instant.parse("2026-10-12T12:00:00Z");
    private static final Instant END = START.plusSeconds(300);
    private static final UUID PATIENT = UUID.randomUUID();

    private EmergencySession pending() {
        return EmergencySession.pending(UUID.randomUUID(), UUID.randomUUID(), PATIENT, START, END);
    }

    @Test void pendingSessionIsRestrictedToItsPatientAndTimeWindow() {
        EmergencySession session = pending();
        assertFalse(session.isActiveAt(START.minusNanos(1)));
        assertTrue(session.isActiveAt(START));
        assertTrue(session.permitsPatient(PATIENT, END.minusNanos(1)));
        assertFalse(session.permitsPatient(UUID.randomUUID(), START));
        assertFalse(session.isActiveAt(END));
    }

    @Test void approvalPreservesScopeAndExpiry() {
        EmergencySession session = pending();
        EmergencySession approved = session.approve(START);
        assertEquals(EmergencySession.Status.APPROVED, approved.status());
        assertEquals(session.patientId(), approved.patientId());
        assertEquals(session.expiresAt(), approved.expiresAt());
        assertThrows(IllegalStateException.class, () -> approved.approve(START));
    }

    @Test void denialAndRevocationImmediatelyInvalidate() {
        EmergencySession denied = pending().deny(START);
        assertFalse(denied.isActiveAt(START));
        assertThrows(IllegalStateException.class, () -> denied.approve(START));
        EmergencySession revoked = pending().revoke(START);
        assertFalse(revoked.isActiveAt(START));
        assertThrows(IllegalStateException.class, () -> revoked.approve(START));
        assertFalse(pending().approve(START).revoke(START).isActiveAt(START));
    }

    @Test void expiryIsBoundarySafeAndTerminal() {
        EmergencySession session = pending();
        assertThrows(IllegalStateException.class, () -> session.expire(START));
        EmergencySession expired = session.expire(END);
        assertEquals(EmergencySession.Status.EXPIRED, expired.status());
        assertFalse(expired.isActiveAt(END));
        assertSame(expired, expired.expire(END.plusSeconds(1)));
        assertThrows(IllegalStateException.class, () -> session.approve(END));
        assertThrows(IllegalStateException.class, () -> session.deny(END));
        assertThrows(IllegalStateException.class, () -> session.revoke(END));
    }

    @Test void invalidConstructionAndNullTimeFailClosed() {
        assertThrows(IllegalArgumentException.class,
            () -> EmergencySession.pending(UUID.randomUUID(), UUID.randomUUID(), PATIENT, START, START));
        assertThrows(NullPointerException.class, () -> pending().isActiveAt(null));
        assertThrows(NullPointerException.class, () -> pending().approve(null));
    }
}
