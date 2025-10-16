package com.quetoquenana.userservice.security;

import com.quetoquenana.userservice.config.SecurityConfig;
import com.quetoquenana.userservice.controller.UserController;
import com.quetoquenana.userservice.service.UserService;
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

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("GET /api/users returns 401 when unauthenticated")
    void getAllUsers_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/users/page returns 401 when unauthenticated")
    void getUsersPage_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/users/page"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/users/{id} returns 401 when unauthenticated")
    void getUserById_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/users/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/users returns 401 when unauthenticated")
    void createUser_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/users returns 403 for USER role")
    @WithMockUser(username = "user")
    void getAllUsers_UserRole_Returns403() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/users returns 403 for AUDITOR role")
    @WithMockUser(username = "auditor", roles = {"AUDITOR"})
    void createUser_AuditorRole_Returns403() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} returns 403 for USER role")
    @WithMockUser(username = "user", roles = {"USER"})
    void deleteUser_UserRole_Returns403() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/users/{id}/reset-password returns 403 for USER role")
    @WithMockUser(username = "user", roles = {"USER"})
    void resetPassword_UserRole_Returns403() throws Exception {
        mockMvc.perform(post("/api/users/{id}/reset-password", "00000000-0000-0000-0000-000000000000")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newPassword\":\"x\"}"))
                .andExpect(status().isForbidden());
    }
}

