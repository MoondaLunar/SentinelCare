package com.sentinelcare.security;

import com.sentinelcare.entity.Patient;
import com.sentinelcare.repository.PatientRepository;
import com.sentinelcare.service.PatientAuthorizationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PatientObjectAuthorizationTest {
    private final PatientRepository repository = mock(PatientRepository.class);
    private final PatientAuthorizationService authorization = new PatientAuthorizationService(repository);

    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test void forgedAssignedClinicianDoesNotAuthorizePatient() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("clinician-a", null,
                List.of(new SimpleGrantedAuthority("ROLE_CLINICIAN"))));
        Patient stored = new Patient("Stored", LocalDate.of(1980, 1, 1), "Example", "");
        stored.setAssignedClinician("clinician-b");
        when(repository.findById(42L)).thenReturn(Optional.of(stored));

        Patient attackerControlled = mock(Patient.class);
        when(attackerControlled.getId()).thenReturn(42L);
        assertFalse(authorization.canAccessPatient(attackerControlled));
        assertThrows(SecurityException.class, () -> authorization.loadAuthorizedPatient(42L));
    }

    @Test void transientPatientCannotBeAuthorized() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("clinician-a", null,
                List.of(new SimpleGrantedAuthority("ROLE_CLINICIAN"))));
        Patient submitted = new Patient("Submitted", LocalDate.of(1990, 1, 1), "Example", "");
        submitted.setAssignedClinician("clinician-a");
        assertFalse(authorization.canAccessPatient(submitted));
        assertFalse(authorization.canAccessPatient((Long) null));
    }
}
