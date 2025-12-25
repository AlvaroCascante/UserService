package com.quetoquenana.userservice.security;

import com.quetoquenana.userservice.config.CorsConfigProperties;
import com.quetoquenana.userservice.config.RsaKeyProperties;
import com.quetoquenana.userservice.config.SecurityConfig;
import com.quetoquenana.userservice.controller.DefaultDataController;
import com.quetoquenana.userservice.dto.DefaultDataCreateRequest;
import com.quetoquenana.userservice.repository.AppRoleUserRepository;
import com.quetoquenana.userservice.repository.ApplicationRepository;
import com.quetoquenana.userservice.repository.UserRepository;
import com.quetoquenana.userservice.service.DefaultDataService;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DefaultDataController.class)
@Import(SecurityConfig.class)
class DefaultDataControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DefaultDataService defaultDataService;

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
        JsonPayloadToObjectBuilder<DefaultDataCreateRequest> mapper = new JsonPayloadToObjectBuilder<>(DefaultDataCreateRequest.class);
        payload = mapper.loadJsonData("payloads/default-data-create-request.json");

        when(corsConfigProperties.getHosts()).thenReturn("http://localhost");
        when(corsConfigProperties.getMethods()).thenReturn("GET,POST,PUT,DELETE");
        when(corsConfigProperties.getHeaders()).thenReturn("Content-Type,Authorization");
    }

    @Test
    @DisplayName("GET /api/default-data/page returns 401 when unauthenticated")
    void getDefaultDataPage_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/default-data/page"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/default-data/{id} returns 401 when unauthenticated")
    void getDefaultDataById_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/default-data/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/default-data/name/{name} returns 401 when unauthenticated")
    void getDefaultDataByName_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/default-data/page/category/{category}", "abc"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/default-data/page returns 403 when forbidden")
    @WithMockUser(username = "user")
    void getDefaultDataPage_Unauthenticated_Returns403() throws Exception {
        mockMvc.perform(get("/api/default-data/page"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/default-data/{id} returns 403 when forbidden")
    @WithMockUser(username = "user")
    void getDefaultDataById_Unauthenticated_Returns403() throws Exception {
        mockMvc.perform(get("/api/default-data/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/default-data/name/{name} returns 403 when forbidden")
    @WithMockUser(username = "user")
    void getDefaultDataByName_Unauthenticated_Returns403() throws Exception {
        mockMvc.perform(get("/api/default-data/page/category/{category}", "abc"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/default-data returns 401 when unauthenticated")
    void createDefaultData_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(post("/api/default-data")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/default-data/{id} returns 401 when unauthenticated")
    void updateDefaultData_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(put("/api/default-data/{id}", "00000000-0000-0000-0000-000000000000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/default-data/{id} returns 401 when unauthenticated")
    void deleteDefaultData_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(delete("/api/default-data/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/default-data returns 403 for USER role")
    @WithMockUser(username = "user")
    void createDefaultData_UserRole_Returns403() throws Exception {
        mockMvc.perform(post("/api/default-data")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/default-data returns 201 for SYSTEM role")
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void createDefaultData_SystemRole_Returns201() throws Exception {
        mockMvc.perform(post("/api/default-data")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PUT /api/default-data/{id} returns 403 for USER role")
    @WithMockUser(username = "user")
    void updateDefaultData_UserRole_Returns403() throws Exception {
        mockMvc.perform(put("/api/default-data/{id}", "00000000-0000-0000-0000-000000000000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/default-data/{id} returns 200 for SYSTEM role")
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void updateDefaultData_SystemRole_Returns200() throws Exception {
        mockMvc.perform(put("/api/default-data/{id}", "00000000-0000-0000-0000-000000000000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/default-data/{id} returns 403 for USER role")
    @WithMockUser(username = "user")
    void deleteDefaultData_UserRole_Returns403() throws Exception {
        mockMvc.perform(delete("/api/default-data/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/default-data/{id} returns 204 for SYSTEM role")
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void deleteDefaultData_SystemRole_Returns204() throws Exception {
        mockMvc.perform(delete("/api/default-data/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNoContent());
    }
}
