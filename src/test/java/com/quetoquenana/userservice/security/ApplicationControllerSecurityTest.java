package com.quetoquenana.userservice.security;

import com.quetoquenana.userservice.config.CorsConfigProperties;
import com.quetoquenana.userservice.config.RsaKeyProperties;
import com.quetoquenana.userservice.config.SecurityConfig;
import com.quetoquenana.userservice.controller.ApplicationController;
import com.quetoquenana.userservice.dto.AppRoleCreateRequest;
import com.quetoquenana.userservice.dto.AppRoleUserCreateRequest;
import com.quetoquenana.userservice.dto.ApplicationCreateRequest;
import com.quetoquenana.userservice.model.Application;
import com.quetoquenana.userservice.repository.AppRoleUserRepository;
import com.quetoquenana.userservice.repository.ApplicationRepository;
import com.quetoquenana.userservice.repository.UserRepository;
import com.quetoquenana.userservice.service.ApplicationService;
import com.quetoquenana.userservice.service.SecurityService;
import com.quetoquenana.userservice.util.JsonPayloadToObjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationController.class)
@Import(SecurityConfig.class)
class ApplicationControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationService applicationService;

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
    private String addUserPayload;
    private String addRolePayload;

    @BeforeEach
    void setup() throws IOException, URISyntaxException {
        JsonPayloadToObjectBuilder<ApplicationCreateRequest> mapper = new JsonPayloadToObjectBuilder<>(ApplicationCreateRequest.class);
        payload = mapper.loadJsonData("payloads/application-create-request.json");

        JsonPayloadToObjectBuilder<AppRoleUserCreateRequest> addUserMapper = new JsonPayloadToObjectBuilder<>(AppRoleUserCreateRequest.class);
        addUserPayload = addUserMapper.loadJsonData("payloads/app-user-create-request.json");

        JsonPayloadToObjectBuilder<AppRoleCreateRequest> addRoleMapper = new JsonPayloadToObjectBuilder<>(AppRoleCreateRequest.class);
        addRolePayload = addRoleMapper.loadJsonData("payloads/app-role-create-request.json");

        when(corsConfigProperties.getHosts()).thenReturn("http://localhost");
        when(corsConfigProperties.getMethods()).thenReturn("GET,POST,PUT,DELETE");
        when(corsConfigProperties.getHeaders()).thenReturn("Content-Type,Authorization");

        when(securityService.canAccessIdPerson(any(), any())).thenReturn(true);
        when(securityService.canAccessIdAddress(any(), any())).thenReturn(true);
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
    @DisplayName("GET /api/applications/search returns 401 when unauthenticated")
    void searchApplications_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/applications/search").param("name", "abc"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/applications returns 401 when unauthenticated")
    void createApplication_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("UPDATE /api/applications/{id} returns 401 when unauthenticated")
    void updateApplication_UserRole_Returns401() throws Exception {
        mockMvc.perform(put("/api/applications/{id}", "00000000-0000-0000-0000-000000000000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/applications/{id} returns 401 when unauthenticated")
    void deleteApplication_UserRole_Returns401() throws Exception {
        mockMvc.perform(delete("/api/applications/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/applications/page returns 403 when forbidden")
    @WithMockUser(username = "user")
    void getApplicationsPage_Unauthenticated_Returns403() throws Exception {
        mockMvc.perform(get("/api/applications/page"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/applications/{id} returns 403 when forbidden")
    @WithMockUser(username = "user")
    void getApplicationById_Unauthenticated_Returns403() throws Exception {
        mockMvc.perform(get("/api/applications/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/applications/search returns 403 when forbidden")
    @WithMockUser(username = "user")
    void searchApplications_Unauthenticated_Returns403() throws Exception {
        mockMvc.perform(get("/api/applications/search").param("name", "abc"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/applications returns 403 when forbidden")
    @WithMockUser(username = "user")
    void createApplication_Unauthenticated_Returns403() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("UPDATE /api/applications/{id} returns 403 when forbidden")
    @WithMockUser(username = "user")
    void updateApplication_UserRole_Returns403() throws Exception {
        mockMvc.perform(put("/api/applications/{id}", "00000000-0000-0000-0000-000000000000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/applications/{id} returns 403 when forbidden")
    @WithMockUser(username = "user")
    void deleteApplication_UserRole_Returns403() throws Exception {
        mockMvc.perform(delete("/api/applications/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isForbidden());
    }
    @Test
    @DisplayName("GET /api/applications/page returns 200")
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void getApplicationsPage_Returns200() throws Exception {
        when(applicationService.findAll(any(Pageable.class))).thenReturn(Page.empty());
        mockMvc.perform(get("/api/applications/page"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/applications/{id} returns 200")
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void getApplicationById_Returns200() throws Exception {
        when(applicationService.findById(any())).thenReturn(Optional.of(new Application()));

        mockMvc.perform(get("/api/applications/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/applications/search returns 200")
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void searchApplications_Returns200() throws Exception {
        when(applicationService.searchByName(anyString(), any(Pageable.class))).thenReturn(Page.empty());
        mockMvc.perform(get("/api/applications/search").param("name", "abc"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/applications returns 200")
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void createApplication_Returns200() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("UPDATE /api/applications/{id} returns 200")
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void updateApplication_Returns200() throws Exception {
        mockMvc.perform(put("/api/applications/{id}", "00000000-0000-0000-0000-000000000000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/applications/{id} returns 204")
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void deleteApplication_Returns204() throws Exception {
        mockMvc.perform(delete("/api/applications/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/applications/{id}/role returns 201")
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void AddRole_Returns201() throws Exception {
        mockMvc.perform(post("/api/applications/{id}/role", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addRolePayload))
                .andExpect(status().isCreated());
    }
    @Test
    @DisplayName("POST /api/applications/{id}/role returns 403 for forbidden")
    @WithMockUser(username = "user")
    void AddRole_Returns403() throws Exception {
        mockMvc.perform(post("/api/applications/{id}/role", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addRolePayload))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/applications/{id}/role returns 401 when unauthenticated")
    void AddRole_Returns401() throws Exception {
        mockMvc.perform(post("/api/applications/{id}/role", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(addRolePayload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/applications/{id}/user returns 201")
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void AddUser_Returns201() throws Exception {
        mockMvc.perform(post("/api/applications/{id}/user", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addUserPayload))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/applications/{id}/user returns 403 for forbidden")
    @WithMockUser(username = "user")
    void AddUser_Returns403() throws Exception {
        mockMvc.perform(post("/api/applications/{id}/user", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addUserPayload))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/applications/{id}/user returns 401 when unauthenticated")
    void AddUser_Returns401() throws Exception {
        mockMvc.perform(post("/api/applications/{id}/user", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addUserPayload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/applications/{id}/role/{roleId} returns 201")
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void DeleteRole_Returns201() throws Exception {
        mockMvc.perform(delete("/api/applications/{id}/role/{roleId}", UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/applications/{id}/role/{roleId} returns 403 for forbidden")
    @WithMockUser(username = "user")
    void DeleteRole_Returns403() throws Exception {
        mockMvc.perform(delete("/api/applications/{id}/role/{roleId}", UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/applications/{id}/role/{roleId} returns 401 when unauthenticated")
    void DeleteRole_Returns401() throws Exception {
        mockMvc.perform(delete("/api/applications/{id}/role/{roleId}", UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/applications/{id}/user/{username} returns 201")
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void DeleteUser_Returns201() throws Exception {
        mockMvc.perform(delete("/api/applications/{id}/user/{username}", UUID.randomUUID(), "someuser"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/applications/{id}/user/{username} returns 403 for forbidden")
    @WithMockUser(username = "user")
    void DeleteUser_Returns403() throws Exception {
        mockMvc.perform(delete("/api/applications/{id}/user/{username}", UUID.randomUUID(), "someuser"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/applications/{id}/user/{username} returns 401 when unauthenticated")
    void DeleteUser_Returns401() throws Exception {
        mockMvc.perform(delete("/api/applications/{id}/user/{username}", UUID.randomUUID(), "someuser"))
                .andExpect(status().isUnauthorized());
    }
}
