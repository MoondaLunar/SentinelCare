package com.sentinelcare.security;

import org.springframework.stereotype.Component;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prototype-only single-process session registry. A frontend must explicitly
 * report genuine user interaction; API reads, polling and background sync do not refresh idle time.
 * Use a shared durable/centralized store before multi-instance deployment.
 */
@Component
public class IdleSessionRegistry {
    public static final Duration IDLE_LIMIT = Duration.ofMinutes(5);
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
    private final Clock clock;

    public IdleSessionRegistry() { this(Clock.systemUTC()); }
    IdleSessionRegistry(Clock clock) { this.clock = clock; }

    public String create(String username) {
        String id = UUID.randomUUID().toString();
        sessions.put(id, new Session(username, clock.instant()));
        return id;
    }

    public boolean valid(String id, String username) {
        if (id == null || username == null) return false;
        Instant now = clock.instant();
        Session session = sessions.computeIfPresent(id, (key, old) ->
            old.username().equals(username) && now.isBefore(old.lastActivity().plus(IDLE_LIMIT)) ? old : null);
        return session != null;
    }

    public boolean activity(String id, String username) {
        if (id == null || username == null) return false;
        Instant now = clock.instant();
        Session session = sessions.computeIfPresent(id, (key, old) ->
            old.username().equals(username) && now.isBefore(old.lastActivity().plus(IDLE_LIMIT))
                ? new Session(username, now) : null);
        return session != null;
    }

    public void revoke(String id) { if (id != null) sessions.remove(id); }
    private record Session(String username, Instant lastActivity) {}
}
