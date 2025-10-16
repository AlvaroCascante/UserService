package com.quetoquenana.personservice.security;

import com.quetoquenana.personservice.config.SecurityConfig;
import com.quetoquenana.personservice.controller.PersonController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.quetoquenana.personservice.service.PersonService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PersonController.class)
@Import(SecurityConfig.class)
class PersonControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersonService personService;

    @Test
    @DisplayName("GET /api/persons returns 401 when unauthenticated")
    void getAllPersons_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/persons"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/persons/page returns 401 when unauthenticated")
    void getPersonsPage_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/persons/page"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/persons/{id} returns 401 when unauthenticated")
    void getPersonById_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/persons/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/persons returns 401 when unauthenticated")
    void createPerson_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(post("/api/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/persons returns 403 for USER role")
    @WithMockUser(username = "user")
    void getAllPersons_UserRole_Returns403() throws Exception {
        mockMvc.perform(get("/api/persons"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/persons returns 403 for AUDITOR role")
    @WithMockUser(username = "auditor", roles = {"AUDITOR"})
    void createPerson_AuditorRole_Returns403() throws Exception {
        mockMvc.perform(post("/api/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/persons/{id} returns 403 for USER role")
    @WithMockUser(username = "user", roles = {"USER"})
    void deletePerson_UserRole_Returns403() throws Exception {
        mockMvc.perform(delete("/api/persons/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isForbidden());
    }
}
