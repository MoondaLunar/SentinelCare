package com.sentinelcare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinelcare.config.SecurityConfig;
import com.sentinelcare.entity.Patient;
import com.sentinelcare.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
    private PatientRepository patientRepository;

    @Test
    @WithMockUser
    void getAllPatientsShouldReturnPatientsFromRepository() throws Exception {
        Patient patient = new Patient("Alice Johnson", LocalDate.of(1988, 3, 10), "Asthma", "Ongoing monitor");
        when(patientRepository.findAll()).thenReturn(List.of(patient));

        mockMvc.perform(get("/api/v1/patients"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Alice Johnson"))
            .andExpect(jsonPath("$[0].diagnosis").value("Asthma"));
    }

    @Test
    @WithMockUser
    void getPatientWhenExistsShouldReturnPatient() throws Exception {
        Patient patient = new Patient("Bob Smith", LocalDate.of(1975, 11, 2), "Diabetes", "Dietary guidance");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        mockMvc.perform(get("/api/v1/patients/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Bob Smith"))
            .andExpect(jsonPath("$.diagnosis").value("Diabetes"));
    }

    @Test
    @WithMockUser
    void getPatientWhenMissingShouldReturnNotFound() throws Exception {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/patients/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void createPatientShouldPersistAndReturnSavedPatient() throws Exception {
        Patient request = new Patient("Carol White", LocalDate.of(1995, 7, 22), "Migraine", "No current issues");
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/v1/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Carol White"))
            .andExpect(jsonPath("$.diagnosis").value("Migraine"));
    }
}
