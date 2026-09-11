package com.sentinelcare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinelcare.config.SecurityConfig;
import com.sentinelcare.entity.Patient;
import com.sentinelcare.security.JwtTokenService;
import com.sentinelcare.service.PatientService;
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

import java.util.Objects;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PatientService patientService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getAllPatientsShouldReturnPatientsFromRepository() throws Exception {
        Patient patient = new Patient("Alice Johnson", LocalDate.of(1988, 3, 10), "Asthma", "Ongoing monitor");
        when(patientService.getVisiblePatients("admin")).thenReturn(List.of(patient));

        mockMvc.perform(get("/api/v1/patients"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Alice Johnson"))
            .andExpect(jsonPath("$[0].diagnosis").value("Asthma"));
    }

    @Test
    @WithMockUser(username = "clinician", roles = "CLINICIAN")
    void getAllPatientsForClinicianShouldReturnOnlyAssignedPatients() throws Exception {
        Patient patient = new Patient("Alice Johnson", LocalDate.of(1988, 3, 10), "Asthma", "Ongoing monitor");
        patient.setAssignedClinician("clinician");
        when(patientService.getVisiblePatients("clinician")).thenReturn(List.of(patient));

        mockMvc.perform(get("/api/v1/patients"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].assignedClinician").value("clinician"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getPatientWhenExistsShouldReturnPatient() throws Exception {
        Patient patient = new Patient("Bob Smith", LocalDate.of(1975, 11, 2), "Diabetes", "Dietary guidance");
        when(patientService.canAccessPatient(1L)).thenReturn(true);
        when(patientService.getPatient(1L)).thenReturn(Optional.of(patient));

        mockMvc.perform(get("/api/v1/patients/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Bob Smith"))
            .andExpect(jsonPath("$.diagnosis").value("Diabetes"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getPatientWhenMissingShouldReturnNotFound() throws Exception {
        when(patientService.canAccessPatient(99L)).thenReturn(true);
        when(patientService.getPatient(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/patients/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createPatientShouldAssignPatientToActingClinician() throws Exception {
        Patient request = new Patient("Carol White", LocalDate.of(1995, 7, 22), "Migraine", "No current issues");
        when(patientService.createPatient(any(Patient.class))).thenAnswer(invocation -> {
            Patient saved = invocation.getArgument(0, Patient.class);
            saved.setAssignedClinician("admin");
            return Objects.requireNonNull(saved);
        });

        mockMvc.perform(post("/api/v1/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Carol White"))
            .andExpect(jsonPath("$.diagnosis").value("Migraine"))
            .andExpect(jsonPath("$.assignedClinician").value("admin"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createPatientWithoutBirthdateShouldReturnValidationProblem() throws Exception {
        mockMvc.perform(post("/api/v1/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"No Birthdate\",\"diagnosis\":\"Dx\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors.birthdate").value("must not be null"));
    }

    @Test
    @WithMockUser(username = "clinician", roles = "CLINICIAN")
    void createPatientWhenClinicianShouldBeForbidden() throws Exception {
        Patient request = new Patient("Carol White", LocalDate.of(1995, 7, 22), "Migraine", "No current issues");

        mockMvc.perform(post("/api/v1/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
            .andExpect(status().isForbidden());
    }
}
