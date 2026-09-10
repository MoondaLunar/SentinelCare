package com.sentinelcare.controller;

import com.sentinelcare.config.SecurityConfig;
import com.sentinelcare.error.ConflictException;
import com.sentinelcare.error.ResourceNotFoundException;
import com.sentinelcare.security.JwtTokenService;
import com.sentinelcare.service.GdprService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GdprController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class GdprControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GdprService gdprService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @WithMockUser(roles = "ADMIN")
    void erasePatientDataWhenNoDependentsShouldReturnDeleted() throws Exception {
        mockMvc.perform(delete("/api/v1/gdpr/patients/42"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("deleted"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void erasePatientDataWhenMissingShouldReturnNotFoundProblem() throws Exception {
        willThrow(new ResourceNotFoundException("Patient not found: 99"))
            .given(gdprService).erasePatientData(99L);

        mockMvc.perform(delete("/api/v1/gdpr/patients/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("Not Found"))
            .andExpect(jsonPath("$.detail").value("Patient not found: 99"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void erasePatientDataWithRetentionDependentsShouldReturnConflictProblem() throws Exception {
        willThrow(new ConflictException("Patient record is retained: 1 consent(s) and 0 consult note(s) exist."))
            .given(gdprService).erasePatientData(42L);

        mockMvc.perform(delete("/api/v1/gdpr/patients/42"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.title").value("Conflict"))
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void anonymizePatientDataShouldReturnRevokedConsentCount() throws Exception {
        when(gdprService.anonymizePatientData(42L)).thenReturn(2);

        mockMvc.perform(post("/api/v1/gdpr/patients/42/anonymize"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("anonymized"))
            .andExpect(jsonPath("$.revokedConsents").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void anonymizePatientDataWhenMissingShouldReturnNotFoundProblem() throws Exception {
        willThrow(new ResourceNotFoundException("Patient not found: 99"))
            .given(gdprService).anonymizePatientData(99L);

        mockMvc.perform(post("/api/v1/gdpr/patients/99/anonymize"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("Patient not found: 99"));
    }
}