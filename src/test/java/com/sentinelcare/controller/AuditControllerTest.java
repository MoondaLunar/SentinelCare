package com.sentinelcare.controller;

import com.sentinelcare.config.SecurityConfig;
import com.sentinelcare.entity.AuditEntry;
import com.sentinelcare.service.AuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditService auditService;

    @Test
    @WithMockUser
    void auditEndpointShouldReturnAuditEntries() throws Exception {
        AuditEntry entry = new AuditEntry("clinician-a", "READ", "Patient", 42L, "Viewed patient record");
        entry = new AuditEntry("clinician-a", "READ", "Patient", 42L, "Viewed patient record");
        when(auditService.getAllEntries()).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/v1/audit"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].actor").value("clinician-a"));
    }

    @Test
    @WithMockUser
    void filteredAuditEndpointShouldReturnByEntityTypeAndId() throws Exception {
        AuditEntry entry = new AuditEntry("clinician-a", "READ", "Patient", 42L, "Viewed patient record");
        when(auditService.getEntriesForEntity("Patient", 42L)).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/v1/audit/Patient/42"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].entityType").value("Patient"));
    }
}
