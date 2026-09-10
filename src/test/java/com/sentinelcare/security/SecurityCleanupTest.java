package com.sentinelcare.security;

import com.sentinelcare.dto.ConsentCreateRequest;
import com.sentinelcare.entity.AuditEntry;
import com.sentinelcare.entity.ConsentRecord;
import com.sentinelcare.entity.ConsentStatus;
import com.sentinelcare.entity.ConsultNote;
import com.sentinelcare.entity.Patient;
import com.sentinelcare.repository.AuditEntryRepository;
import com.sentinelcare.repository.ConsentRecordRepository;
import com.sentinelcare.repository.ConsultNoteRepository;
import com.sentinelcare.repository.PatientRepository;
import com.sentinelcare.service.AuditService;
import com.sentinelcare.service.ConsentService;
import com.sentinelcare.service.ConsultNoteService;
import com.sentinelcare.service.PatientAuthorizationService;
import com.sentinelcare.service.PatientService;
import com.sentinelcare.web.ApiProblemWriter;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityCleanupTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private ConsentRecordRepository consentRecordRepository;

    @Mock
    private ConsultNoteRepository consultNoteRepository;

    @Mock
    private AuditEntryRepository auditEntryRepository;

    @Mock
    private ApiProblemWriter problemWriter;

    @Mock
    private AuditService auditService;

    @Test
    void anonymousUsersCannotAccessPatientData() {
        PatientAuthorizationService authorizationService = new PatientAuthorizationService(patientRepository);
        PatientService patientService = new PatientService(patientRepository, authorizationService);

        SecurityContextHolder.clearContext();

        assertTrue(patientService.getVisiblePatients(null).isEmpty());
        assertTrue(patientService.getVisiblePatients("   ").isEmpty());
        assertFalse(patientService.canAccessPatient(42L));
    }

    @Test
    void clinicianCannotAccessOtherCliniciansPatient() {
        PatientAuthorizationService authorizationService = new PatientAuthorizationService(patientRepository);
        PatientService patientService = new PatientService(patientRepository, authorizationService);

        Patient patient = new Patient("Other patient", LocalDate.of(1980, 1, 1), "Asthma", "notes");
        patient.setAssignedClinician("clinician-b");
        when(patientRepository.findById(99L)).thenReturn(Optional.of(patient));

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                User.withUsername("clinician-a").password("secret").authorities(new SimpleGrantedAuthority("ROLE_CLINICIAN")).build(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CLINICIAN"))
            )
        );

        assertFalse(patientService.canAccessPatient(99L));
        assertTrue(patientService.getVisiblePatients("clinician-a").isEmpty());
    }

    @Test
    void clinicianCannotAccessOtherCliniciansConsentRecords() {
        PatientAuthorizationService authorizationService = new PatientAuthorizationService(patientRepository);
        ConsentService consentService = new ConsentService(consentRecordRepository, patientRepository, authorizationService, auditService);

        Patient patient = new Patient("Other patient", LocalDate.of(1980, 1, 1), "Asthma", "notes");
        patient.setAssignedClinician("clinician-b");
        ConsentRecord record = new ConsentRecord(patient, "treatment", true, "clinic-b");

        when(consentRecordRepository.findByPatientAssignedClinician("clinician-a")).thenReturn(List.of());
        lenient().when(consentRecordRepository.findByPatientAssignedClinician("clinician-b")).thenReturn(List.of(record));

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                User.withUsername("clinician-a").password("secret").authorities(new SimpleGrantedAuthority("ROLE_CLINICIAN")).build(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CLINICIAN"))
            )
        );

        assertTrue(consentService.getAllConsents().isEmpty());
        assertTrue(consentService.getActiveConsents().isEmpty());
        verify(consentRecordRepository, times(2)).findByPatientAssignedClinician("clinician-a");
    }

    @Test
    void clinicianCannotAccessOtherCliniciansConsultNotes() {
        PatientAuthorizationService authorizationService = new PatientAuthorizationService(patientRepository);
        ConsultNoteService consultNoteService = new ConsultNoteService(consultNoteRepository, patientRepository, authorizationService, auditService);

        Patient patient = new Patient("Other patient", LocalDate.of(1980, 1, 1), "Asthma", "notes");
        patient.setAssignedClinician("clinician-b");
        ConsultNote note = new ConsultNote(patient, "Dr. B", "secret note");

        when(consultNoteRepository.findByPatientAssignedClinician("clinician-a")).thenReturn(List.of());
        lenient().when(consultNoteRepository.findByPatientAssignedClinician("clinician-b")).thenReturn(List.of(note));

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                User.withUsername("clinician-a").password("secret").authorities(new SimpleGrantedAuthority("ROLE_CLINICIAN")).build(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CLINICIAN"))
            )
        );

        assertTrue(consultNoteService.getAllNotes().isEmpty());
        verify(consultNoteRepository).findByPatientAssignedClinician("clinician-a");
    }

    @Test
    void clinicianCannotReadAuditForOtherCliniciansPatients() {
        PatientAuthorizationService authorizationService = new PatientAuthorizationService(patientRepository);
        AuditService auditService = new AuditService(auditEntryRepository, authorizationService, patientRepository);

        Patient patient = new Patient("Other patient", LocalDate.of(1980, 1, 1), "Asthma", "notes");
        patient.setAssignedClinician("clinician-b");
        AuditEntry entry = new AuditEntry("clinician-b", "READ", "Patient", 12L, "Viewed patient record");

        when(patientRepository.findByAssignedClinician("clinician-a")).thenReturn(List.of());
        when(patientRepository.findById(12L)).thenReturn(Optional.of(patient));
        lenient().when(auditEntryRepository.findByEntityTypeAndEntityId("Patient", 12L)).thenReturn(List.of(entry));

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                User.withUsername("clinician-a").password("secret").authorities(new SimpleGrantedAuthority("ROLE_CLINICIAN")).build(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CLINICIAN"))
            )
        );

        assertTrue(auditService.getAllEntries().isEmpty());
        assertTrue(auditService.getEntriesForEntity("Patient", 12L).isEmpty());
        verify(patientRepository).findByAssignedClinician("clinician-a");
    }

    @Test
    void invalidJwtFailsCleanly() throws Exception {
        JwtTokenService jwtTokenService = mock(JwtTokenService.class);
        UserDetailsService userDetailsService = username -> User.withUsername(username).password("pw").roles("ADMIN").build();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenService, userDetailsService, problemWriter);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer bad-token");
        when(jwtTokenService.extractUsername("bad-token")).thenThrow(new MalformedJwtException("bad token"));

        filter.doFilter(request, response, chain);

        verify(problemWriter).write(eq(response), eq(HttpStatus.UNAUTHORIZED), eq("Unauthorized"), eq("Invalid or expired JWT"));
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void expiredJwtFailsCleanly() throws Exception {
        JwtTokenService jwtTokenService = mock(JwtTokenService.class);
        UserDetailsService userDetailsService = username -> User.withUsername(username).password("pw").roles("ADMIN").build();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenService, userDetailsService, problemWriter);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer expired-token");
        when(jwtTokenService.extractUsername("expired-token")).thenThrow(new IllegalArgumentException("expired"));

        filter.doFilter(request, response, chain);

        verify(problemWriter).write(eq(response), eq(HttpStatus.UNAUTHORIZED), eq("Unauthorized"), eq("Invalid or expired JWT"));
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void adminRetainsAccessAndMissingAuthenticationDoesNotFallbackToFindAll() {
        PatientAuthorizationService authorizationService = new PatientAuthorizationService(patientRepository);
        PatientService patientService = new PatientService(patientRepository, authorizationService);

        Patient patient = new Patient("Admin patient", LocalDate.of(1975, 5, 9), "Migraine", "notes");
        when(patientRepository.findAll()).thenReturn(List.of(patient));

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                User.withUsername("admin").password("secret").authorities(new SimpleGrantedAuthority("ROLE_ADMIN")).build(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            )
        );

        assertEquals(1, patientService.getVisiblePatients("admin").size());
        assertTrue(patientService.canAccessPatient(1L));

        SecurityContextHolder.clearContext();
        assertTrue(patientService.getVisiblePatients(null).isEmpty());
    }

    @Test
    void consentCreateAuthorizesAgainstDbRowNotRequestData() {
        PatientAuthorizationService authorizationService = new PatientAuthorizationService(patientRepository);
        ConsentService consentService = new ConsentService(consentRecordRepository, patientRepository, authorizationService, auditService);

        Patient patient = new Patient("Other patient", LocalDate.of(1980, 1, 1), "Asthma", "notes");
        patient.setAssignedClinician("clinician-b");
        when(patientRepository.findById(99L)).thenReturn(Optional.of(patient));

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                User.withUsername("clinician-a").password("secret").authorities(new SimpleGrantedAuthority("ROLE_CLINICIAN")).build(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CLINICIAN"))
            )
        );

        ConsentCreateRequest request = new ConsentCreateRequest();
        request.setPatientId(99L);
        request.setConsentType("treatment");
        request.setGranted(true);
        request.setSource("clinic-a");

        assertThrows(SecurityException.class, () -> consentService.createConsent(request));

        verify(patientRepository).findById(99L);
        verify(consentRecordRepository, never()).save(any());
        verify(auditService).recordDenied("CONSENT_CREATE_DENIED", "Patient", 99L,
            "consent creation attempted on unauthorized patient");
    }

    @Test
    void consentCreateSucceedsForAssignedPatientWithServerDerivedStatus() {
        PatientAuthorizationService authorizationService = new PatientAuthorizationService(patientRepository);
        ConsentService consentService = new ConsentService(consentRecordRepository, patientRepository, authorizationService, auditService);

        Patient patient = new Patient("Own patient", LocalDate.of(1980, 1, 1), "Asthma", "notes");
        patient.setAssignedClinician("clinician-a");
        when(patientRepository.findById(99L)).thenReturn(Optional.of(patient));
        when(consentRecordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                User.withUsername("clinician-a").password("secret").authorities(new SimpleGrantedAuthority("ROLE_CLINICIAN")).build(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CLINICIAN"))
            )
        );

        ConsentCreateRequest request = new ConsentCreateRequest();
        request.setPatientId(99L);
        request.setConsentType("treatment");
        request.setGranted(true);
        request.setSource("clinic-a");

        ConsentRecord saved = consentService.createConsent(request);

        assertEquals(ConsentStatus.ACTIVE, saved.getStatus());
        verify(consentRecordRepository).save(any());
        verify(auditService).record(eq("CONSENT_CREATED"), eq("ConsentRecord"), any(), anyString());
    }
}
