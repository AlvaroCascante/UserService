package com.quetoquenana.userservice.security;

import com.quetoquenana.userservice.config.CorsConfigProperties;
import com.quetoquenana.userservice.config.RsaKeyProperties;
import com.quetoquenana.userservice.config.SecurityConfig;
import com.quetoquenana.userservice.controller.ApplicationController;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
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

    @BeforeEach
    void setup() throws IOException, URISyntaxException {
        JsonPayloadToObjectBuilder<ApplicationCreateRequest> mapper = new JsonPayloadToObjectBuilder<>(ApplicationCreateRequest.class);
        payload = mapper.loadJsonData("payloads/application-create-request.json");

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
    @DisplayName("GET /api/applications/name/{name} returns 401 when unauthenticated")
    void getApplicationByName_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/applications/name/{name}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/applications/search returns 401 when unauthenticated")
    void searchApplications_Unauthenticated_Returns401() throws Exception {
        // Use query param instead of malformed path
        mockMvc.perform(get("/api/applications/search").param("name", "abc"))
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
        JsonPayloadToObjectBuilder<Object> mapper = new JsonPayloadToObjectBuilder<>(Object.class);
        String payload = mapper.loadJsonData("payloads/application-create-request.json");

        mockMvc.perform(post("/api/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/applications/{id} returns 403 for USER role")
    @WithMockUser(username = "user", roles = {"USER"})
    void deleteApplication_UserRole_Returns403() throws Exception {
        mockMvc.perform(delete("/api/applications/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/applications returns 201 for SYSTEM role")
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void createApplication_SystemRole_Returns201() throws Exception {
        JsonPayloadToObjectBuilder<Object> mapper = new JsonPayloadToObjectBuilder<>(Object.class);
        String payload = mapper.loadJsonData("payloads/application-create-request.json");

        mockMvc.perform(post("/api/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated());
    }
}
