package com.quetoquenana.userservice.security;

import com.quetoquenana.userservice.config.CorsConfigProperties;
import com.quetoquenana.userservice.config.RsaKeyProperties;
import com.quetoquenana.userservice.config.SecurityConfig;
import com.quetoquenana.userservice.controller.ProfileController;
import com.quetoquenana.userservice.dto.ProfileCreateRequest;
import com.quetoquenana.userservice.dto.ProfileUpdateRequest;
import com.quetoquenana.userservice.model.Profile;
import com.quetoquenana.userservice.repository.AppRoleUserRepository;
import com.quetoquenana.userservice.repository.ApplicationRepository;
import com.quetoquenana.userservice.repository.UserRepository;
import com.quetoquenana.userservice.service.ProfileService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@Import(SecurityConfig.class)
class ProfileControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfileService profileService;

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

    private static final String PROFILE_PAYLOAD = "{\"gender\":\"M\"}";
    private static final String PERSON_ID = "00000000-0000-0000-0000-000000000000";
    private static final String PROFILE_ID = "00000000-0000-0000-0000-000000000001";
    private String payload;

    @BeforeEach
    void setup() throws IOException, URISyntaxException {
        JsonPayloadToObjectBuilder<ProfileCreateRequest> mapper = new JsonPayloadToObjectBuilder<>(ProfileCreateRequest.class);
        payload = mapper.loadJsonData("payloads/profile-create-request.json");

        when(corsConfigProperties.getHosts()).thenReturn("http://localhost");
        when(corsConfigProperties.getMethods()).thenReturn("GET,POST,PUT,DELETE");
        when(corsConfigProperties.getHeaders()).thenReturn("Content-Type,Authorization");

        when(securityService.canAccessIdPerson(any(), any())).thenReturn(true);
        when(securityService.canAccessIdProfile(any(), any())).thenReturn(true);
    }

    @Test
    @DisplayName("POST /api/persons/{id}/profile returns 401 when unauthenticated")
    void addProfile_Unauthenticated_Returns401() throws Exception {
        when(securityService.canAccessIdPerson(any(), any())).thenReturn(false);
        mockMvc.perform(post("/api/persons/" + PERSON_ID + "/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/persons/profile/{idProfile} returns 401 when unauthenticated")
    void updateProfile_Unauthenticated_Returns401() throws Exception {
        when(securityService.canAccessIdProfile(any(), any())).thenReturn(false);
        mockMvc.perform(put("/api/persons/profile/" + PROFILE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isUnauthorized());
    }



    @Test
    @DisplayName("POST /api/persons/{id}/profile returns 200")
    void addProfile_Unauthenticated_Returns200() throws Exception {
        mockMvc.perform(post("/api/persons/" + PERSON_ID + "/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/persons/profile/{idProfile} returns 200")
    void updateProfile_Unauthenticated_Returns200() throws Exception {
        mockMvc.perform(put("/api/persons/profile/" + PROFILE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }
}
