package com.sentinelcare.security;

import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class IdleSessionRegistryTest {
    @Test void idleSessionExpiresEvenWhenRequestsAreValidated() {
        MutableClock clock = new MutableClock();
        IdleSessionRegistry registry = new IdleSessionRegistry(clock);
        String id = registry.create("clinician");
        clock.advanceSeconds(299);
        assertTrue(registry.valid(id, "clinician"));
        clock.advanceSeconds(1);
        assertFalse(registry.valid(id, "clinician"));
        assertFalse(registry.activity(id, "clinician"));
    }

    @Test void onlyExplicitActivityRenewsIdleDeadline() {
        MutableClock clock = new MutableClock();
        IdleSessionRegistry registry = new IdleSessionRegistry(clock);
        String id = registry.create("clinician");
        clock.advanceSeconds(240);
        assertTrue(registry.activity(id, "clinician"));
        clock.advanceSeconds(240);
        assertTrue(registry.valid(id, "clinician"));
        assertFalse(registry.valid(id, "other-user"));
        registry.revoke(id);
        assertFalse(registry.valid(id, "clinician"));
    }

    private static final class MutableClock extends Clock {
        private final AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-01-01T00:00:00Z"));
        void advanceSeconds(long seconds) { now.updateAndGet(t -> t.plusSeconds(seconds)); }
        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return now.get(); }
    }
}
