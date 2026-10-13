package com.sentinelcare.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmergencyAccessPolicyTest {
    @Test void summaryRequiresVerifiedScopedSession() {
        assertTrue(EmergencyAccessPolicy.mayReadSummary(true, true, true));
        assertFalse(EmergencyAccessPolicy.mayReadSummary(false, true, true));
        assertFalse(EmergencyAccessPolicy.mayReadSummary(true, false, true));
        assertFalse(EmergencyAccessPolicy.mayReadSummary(true, true, false));
    }

    @Test void onlyExplicitCriticalCareDepartmentsWithPermissionMayWrite() {
        for (String department : new String[]{"ER", "ICU", "NICU", "PICU"}) {
            assertTrue(EmergencyAccessPolicy.mayCreateUrgentNote(true, true, true, department, true));
            assertFalse(EmergencyAccessPolicy.mayCreateUrgentNote(true, true, true, department, false));
        }
        for (String department : new String[]{"WARD", "ADMIN", "", "er"}) {
            assertFalse(EmergencyAccessPolicy.mayCreateUrgentNote(true, true, true, department, true));
        }
        assertFalse(EmergencyAccessPolicy.mayCreateUrgentNote(true, true, true, null, true));
    }

    @Test void criticalCareDesignationDoesNotBypassIdentityOrPatientScope() {
        assertFalse(EmergencyAccessPolicy.mayCreateUrgentNote(false, true, true, "NICU", true));
        assertFalse(EmergencyAccessPolicy.mayCreateUrgentNote(true, false, true, "NICU", true));
        assertFalse(EmergencyAccessPolicy.mayCreateUrgentNote(true, true, false, "NICU", true));
    }
}
