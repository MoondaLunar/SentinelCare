package com.sentinelcare.controller;

import com.sentinelcare.config.SecurityConfig;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
    void erasePatientDataWhenFoundShouldReturnCompleted() throws Exception {
        when(gdprService.erasePatientData(42L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/gdpr/patients/42"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("completed"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void erasePatientDataWhenMissingShouldReturnNotFound() throws Exception {
        when(gdprService.erasePatientData(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/v1/gdpr/patients/99"))
            .andExpect(status().isNotFound());
    }
}
