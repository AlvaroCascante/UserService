package com.quetoquenana.userservice.security;

import com.quetoquenana.userservice.config.CorsConfigProperties;
import com.quetoquenana.userservice.config.RsaKeyProperties;
import com.quetoquenana.userservice.config.SecurityConfig;
import com.quetoquenana.userservice.controller.UserController;
import com.quetoquenana.userservice.dto.UserCreateRequest;
import com.quetoquenana.userservice.repository.AppRoleUserRepository;
import com.quetoquenana.userservice.repository.ApplicationRepository;
import com.quetoquenana.userservice.repository.UserRepository;
import com.quetoquenana.userservice.service.UserService;
import com.quetoquenana.userservice.service.SecurityService;
import com.quetoquenana.userservice.util.JsonPayloadToObjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    // Mock beans required by SecurityConfig
    @MockBean
    private CorsConfigProperties corsConfigProperties;

    @MockBean
    private RsaKeyProperties rsaKeyProperties;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ApplicationRepository applicationRepository;

    @MockBean
    private AppRoleUserRepository appRoleUserRepository;

    // Provide JwtDecoder/JwtEncoder mocks to avoid SecurityConfig creating real encoders/decoders
    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private JwtEncoder jwtEncoder;

    // Mock SecurityService bean name for method-security SpEL if needed
    @MockBean(name = "securityService")
    private SecurityService securityService;

    private String payload;

    @BeforeEach
    void setup() throws IOException, URISyntaxException {
        JsonPayloadToObjectBuilder<UserCreateRequest> mapper = new JsonPayloadToObjectBuilder<>(UserCreateRequest.class);
        payload = mapper.loadJsonData("payloads/user-update-request.json");

        when(corsConfigProperties.getHosts()).thenReturn("http://localhost");
        when(corsConfigProperties.getMethods()).thenReturn("GET,POST,PUT,DELETE");
        when(corsConfigProperties.getHeaders()).thenReturn("Content-Type,Authorization");

        when(securityService.canAccessIdPerson(any(), any())).thenReturn(true);
        when(securityService.canAccessIdUser(any(), any())).thenReturn(true);
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
        when(securityService.canAccessIdUser(any(), any())).thenReturn(false);
        mockMvc.perform(get("/api/users/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/users/username/{username} returns 403 for USER role")
    @WithMockUser(username = "user")
    void getAllUsers_UserRole_Returns403() throws Exception {
        mockMvc.perform(get("/api/users/username/{username}", "someuser"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/users/{id} returns 403 for AUDITOR role")
    @WithMockUser(username = "auditor", roles = {"AUDITOR"})
    void updateUser_AuditorRole_Returns403() throws Exception {
        mockMvc.perform(put("/api/users/{id}", "00000000-0000-0000-0000-000000000000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
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
