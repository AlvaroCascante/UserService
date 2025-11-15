package com.quetoquenana.userservice.security;

import com.quetoquenana.userservice.config.CorsConfigProperties;
import com.quetoquenana.userservice.config.RsaKeyProperties;
import com.quetoquenana.userservice.config.SecurityConfig;
import com.quetoquenana.userservice.controller.AddressController;
import com.quetoquenana.userservice.dto.AddressCreateRequest;
import com.quetoquenana.userservice.repository.AppRoleUserRepository;
import com.quetoquenana.userservice.repository.ApplicationRepository;
import com.quetoquenana.userservice.repository.UserRepository;
import com.quetoquenana.userservice.service.AddressService;
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
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AddressController.class)
@Import(SecurityConfig.class)
class AddressControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AddressService addressService;

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

    // Mock SecurityService used by @PreAuthorize SpEL
    @MockBean(name = "securityService")
    private SecurityService securityService;

    private String payload;

    @BeforeEach
    void setup() throws IOException, URISyntaxException {
        JsonPayloadToObjectBuilder<AddressCreateRequest> mapper = new JsonPayloadToObjectBuilder<>(AddressCreateRequest.class);
        payload = mapper.loadJsonData("payloads/address-create-request.json");

        // Provide minimal CORS config values expected by SecurityConfig
        when(corsConfigProperties.getHosts()).thenReturn("http://localhost");
        when(corsConfigProperties.getMethods()).thenReturn("GET,POST,PUT,DELETE");
        when(corsConfigProperties.getHeaders()).thenReturn("Content-Type,Authorization");

        // Make securityService allow access for authenticated tests
        // use any() for the second arg because controller passes UUID (not String) into SpEL
        when(securityService.canAccessIdPerson(any(), any())).thenReturn(true);
        when(securityService.canAccessIdAddress(any(), any())).thenReturn(true);
    }

    private static final String PERSON_ID = "00000000-0000-0000-0000-000000000000";
    private static final String ADDRESS_ID = "00000000-0000-0000-0000-000000000001";

    @Test
    @DisplayName("POST /api/persons/{id}/address returns 401 when unauthenticated")
    void addAddress_Unauthenticated_Returns401() throws Exception {
        when(securityService.canAccessIdPerson(any(), any())).thenReturn(false);

        mockMvc.perform(post("/api/persons/" + PERSON_ID + "/address")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/persons/address/{id} returns 401 when unauthenticated")
    void updateAddress_Unauthenticated_Returns401() throws Exception {
        when(securityService.canAccessIdAddress(any(), any())).thenReturn(false);

        mockMvc.perform(put("/api/persons/address/" + ADDRESS_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/persons/address/{id} returns 401 when unauthenticated")
    void deleteAddress_Unauthenticated_Returns401() throws Exception {
        when(securityService.canAccessIdAddress(any(), any())).thenReturn(false);

        mockMvc.perform(delete("/api/persons/address/" + ADDRESS_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/persons/{id}/address returns 200")
    void addAddress_UserRole_Returns200() throws Exception {
        mockMvc.perform(post("/api/persons/" + PERSON_ID + "/address")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/persons/address/{id} returns 204")
    void deleteAddress_UserRole_Returns204() throws Exception {
        mockMvc.perform(delete("/api/persons/address/" + ADDRESS_ID))
                .andExpect(status().isNoContent());
    }
}
