package com.sentinelcare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinelcare.config.SecurityConfig;
import com.sentinelcare.dto.AuthRequest;
import com.sentinelcare.dto.AuthResponse;
import com.sentinelcare.security.JwtTokenService;
import com.sentinelcare.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @WithMockUser
    void loginShouldReturnJwtTokenForValidCredentials() throws Exception {
        AuthRequest request = new AuthRequest("admin", "admin");
        when(authService.login(any(AuthRequest.class))).thenReturn(new AuthResponse("jwt-token", "admin"));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token"))
            .andExpect(jsonPath("$.username").value("admin"));
    }
}
