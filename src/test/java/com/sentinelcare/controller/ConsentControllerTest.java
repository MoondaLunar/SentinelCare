package com.sentinelcare.controller;

import com.sentinelcare.config.SecurityConfig;
import com.sentinelcare.entity.ConsentRecord;
import com.sentinelcare.entity.ConsentStatus;
import com.sentinelcare.entity.Patient;
import com.sentinelcare.security.JwtTokenService;
import com.sentinelcare.service.ConsentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConsentController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class ConsentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConsentService consentService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getActiveConsentsShouldReturnOnlyActiveConsentRecords() throws Exception {
        Patient patient = new Patient("Alice Johnson", LocalDate.of(1988, 3, 10), "Asthma", "monitor");
        patient.setAssignedClinician("clinician-a");

        ConsentRecord record = new ConsentRecord(patient, "treatment", true, "clinic-a");
        record.setStatus(ConsentStatus.ACTIVE);
        when(consentService.getActiveConsents()).thenReturn(List.of(record));

        mockMvc.perform(get("/api/v1/consents/active"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].consentType").value("treatment"))
            .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void revokeConsentShouldReturnRevokedConsent() throws Exception {
        Patient patient = new Patient("Alice Johnson", LocalDate.of(1988, 3, 10), "Asthma", "monitor");
        patient.setAssignedClinician("clinician-a");

        ConsentRecord record = new ConsentRecord(patient, "treatment", false, "clinic-a");
        record.setStatus(ConsentStatus.REVOKED);
        when(consentService.revokeConsent(42L)).thenReturn(record);

        mockMvc.perform(post("/api/v1/consents/42/revoke"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.consentType").value("treatment"))
            .andExpect(jsonPath("$.status").value("REVOKED"));
    }
}
