package com.sentinelcare.security;

import java.util.Set;

/**
 * Fail-closed policy for temporary break-glass sessions.
 * This policy does NOT issue codes, authenticate users, grant patient access,
 * or persist audit events. Callers must verify those separately.
 */
public final class EmergencyAccessPolicy {
    public static final String EMERGENCY_SUMMARY_READ = "EMERGENCY_SUMMARY_READ";
    public static final String EMERGENCY_NOTE_CREATE = "EMERGENCY_NOTE_CREATE";
    public static final Set<String> NOTE_ENABLED_DEPARTMENTS = Set.of("ER", "ICU", "NICU", "PICU");

    private EmergencyAccessPolicy() {}

    public static boolean mayReadSummary(boolean verifiedIdentity, boolean validEmergencySession,
                                         boolean patientInScope) {
        return verifiedIdentity && validEmergencySession && patientInScope;
    }

    public static boolean mayCreateUrgentNote(boolean verifiedIdentity, boolean validEmergencySession,
                                               boolean patientInScope, String assignedDepartment,
                                               boolean emergencyNotePermission) {
        return mayReadSummary(verifiedIdentity, validEmergencySession, patientInScope)
            && emergencyNotePermission
            && NOTE_ENABLED_DEPARTMENTS.contains(assignedDepartment);
    }
}
