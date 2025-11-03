package com.quetoquenana.userservice.security;

import com.quetoquenana.userservice.config.SecurityConfig;
import com.quetoquenana.userservice.controller.ApplicationController;
import com.quetoquenana.userservice.service.ApplicationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApplicationController.class)
@Import(SecurityConfig.class)
class ApplicationControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationService applicationService;

    @Test
    @DisplayName("GET /api/applications returns 401 when unauthenticated")
    void getAllApplications_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/applications/page returns 401 when unauthenticated")
    void getApplicationsPage_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/applications/page"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/applications/{id} returns 401 when unauthenticated")
    void getApplicationById_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/applications/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/applications returns 401 when unauthenticated")
    void createApplication_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(post("/api/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/applications returns 403 for USER role")
    @WithMockUser(username = "user")
    void getAllApplications_UserRole_Returns403() throws Exception {
        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/applications/{id} returns 403 for USER role")
    @WithMockUser(username = "user", roles = {"USER"})
    void deleteApplication_UserRole_Returns403() throws Exception {
        mockMvc.perform(delete("/api/applications/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isForbidden());
    }
}

