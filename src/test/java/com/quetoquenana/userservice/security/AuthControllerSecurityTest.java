package com.quetoquenana.userservice.security;

import com.quetoquenana.userservice.config.CorsConfigProperties;
import com.quetoquenana.userservice.config.RsaKeyProperties;
import com.quetoquenana.userservice.config.SecurityConfig;
import com.quetoquenana.userservice.controller.AuthController;
import com.quetoquenana.userservice.dto.TokenResponse;
import com.quetoquenana.userservice.dto.UserCreateFromFirebaseResponse;
import com.quetoquenana.userservice.repository.AppRoleUserRepository;
import com.quetoquenana.userservice.repository.ApplicationRepository;
import com.quetoquenana.userservice.repository.UserRepository;
import com.quetoquenana.userservice.service.AuthUserService;
import com.quetoquenana.userservice.service.SecurityService;
import com.quetoquenana.userservice.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static com.quetoquenana.userservice.util.Constants.Headers.APP_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean(name = "securityService")
    private SecurityService securityService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private AuthUserService authUserService;

    @MockitoBean
    private CorsConfigProperties corsConfigProperties;

    @MockitoBean
    private RsaKeyProperties rsaKeyProperties;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ApplicationRepository applicationRepository;

    @MockitoBean
    private AppRoleUserRepository appRoleUserRepository;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean(name = "refreshJwtDecoder")
    private JwtDecoder refreshJwtDecoder;

    @MockitoBean
    private JwtEncoder jwtEncoder;

    @BeforeEach
    void setUp() {
        when(corsConfigProperties.getHosts()).thenReturn("http://localhost");
        when(corsConfigProperties.getMethods()).thenReturn("GET,POST,PUT,DELETE");
        when(corsConfigProperties.getHeaders()).thenReturn("Content-Type,Authorization,X-Application-Name");
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password returns 204 without authentication")
    void forgotPassword_PermitAll_Returns204() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alvaro@example.com"}
                                """))
                .andExpect(status().isNoContent());

        verify(securityService).forgotPassword("alvaro@example.com");
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password returns 400 for invalid payload")
    void forgotPassword_InvalidPayload_Returns400() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":""}
                                """))
                .andExpect(status().isBadRequest());

        verify(securityService, never()).forgotPassword(any());
    }

    @Test
    @DisplayName("POST /api/auth/refresh returns 400 when app header is missing")
    void refresh_MissingHeader_Returns400() throws Exception {
        when(refreshJwtDecoder.decode("refresh-token")).thenReturn(buildRefreshJwt());

        mockMvc.perform(post("/api/auth/refresh")
                        .header("Authorization", "Bearer refresh-token"))
                .andExpect(status().isBadRequest());

        verify(tokenService, never()).refresh(any(), any());
    }

    @Test
    @DisplayName("POST /api/auth/refresh returns 401 when bearer token is missing")
    void refresh_MissingBearerToken_Returns401() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .header(APP_NAME, "USR"))
                .andExpect(status().isUnauthorized());

        verify(tokenService, never()).refresh(any(), any());
    }

    @Test
    @DisplayName("POST /api/auth/refresh returns 200 for valid bearer refresh token")
    void refresh_BearerAuthentication_Returns200() throws Exception {
        when(refreshJwtDecoder.decode("refresh-token")).thenReturn(buildRefreshJwt());
        when(tokenService.refresh(any(), eq("USR"))).thenReturn(new TokenResponse("access", "refresh", 3600L));

        mockMvc.perform(post("/api/auth/refresh")
                        .header(APP_NAME, "USR")
                        .header("Authorization", "Bearer refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh"))
                .andExpect(jsonPath("$.expiresIn").value(3600));

        verify(tokenService).refresh(any(), eq("USR"));
    }

    @Test
    @DisplayName("POST /api/auth/login returns 401 when unauthenticated")
    void login_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .header(APP_NAME, "USR"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login returns 200 for authenticated user")
    @WithMockUser(username = "alvaro@example.com")
    void login_Authenticated_Returns200() throws Exception {
        when(tokenService.createTokens(any(), eq("USR"))).thenReturn(new TokenResponse("access", "refresh", 3600L));

        mockMvc.perform(post("/api/auth/login")
                        .header(APP_NAME, "USR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"));

        verify(securityService).login(any());
        verify(tokenService).createTokens(any(), eq("USR"));
    }

    @Test
    @DisplayName("POST /api/auth/reset returns 204 for authenticated user")
    @WithMockUser(username = "alvaro@example.com")
    void reset_Authenticated_Returns204() throws Exception {
        mockMvc.perform(post("/api/auth/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newPassword":"NewPassword123!"}
                                """))
                .andExpect(status().isNoContent());

        verify(securityService).resetUser(any(), any());
    }

    @Test
    @DisplayName("POST /api/auth/firebase-registration returns 201 for authenticated user")
    @WithMockUser(username = "firebase-user@example.com")
    void firebaseRegistration_Authenticated_Returns201() throws Exception {
        when(authUserService.createFromFirebase(any(), eq("USR"))).thenReturn(new UserCreateFromFirebaseResponse(
                "123456879",
                "1-2345-6789",
                "Alvaro",
                "Cascante",
                "firebase-user@example.com",
                "alvarito",
                "User Service",
                "USR"
        ));
        when(tokenService.createTokensForUser("firebase-user@example.com", "USR")).thenReturn(new TokenResponse("access", "refresh", 3600L));

        mockMvc.perform(post("/api/auth/firebase-registration")
                        .header(APP_NAME, "USR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "person": {
                                    "idNumber": "1-2345-6789",
                                    "name": "Alvaro",
                                    "lastname": "Cascante"
                                  },
                                  "nickname": "alvarito"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.errorCode").value(0));

        verify(authUserService).createFromFirebase(any(), eq("USR"));
        verify(tokenService).createTokensForUser("firebase-user@example.com", "USR");
    }

    @Test
    @DisplayName("GET /api/auth/firebase-login returns 201 for authenticated user")
    @WithMockUser(username = "firebase-user@example.com")
    void firebaseLogin_Authenticated_Returns201() throws Exception {
        when(authUserService.getFirebaseSession("USR")).thenReturn(new UserCreateFromFirebaseResponse(
                "123456879",
                "1-2345-6789",
                "Alvaro",
                "Cascante",
                "firebase-user@example.com",
                "alvarito",
                "User Service",
                "USR"
        ));
        when(tokenService.createTokensForUser("firebase-user@example.com", "USR"))
                .thenReturn(new TokenResponse("access", "refresh", 3600L));

        mockMvc.perform(get("/api/auth/firebase-login")
                        .header(APP_NAME, "USR"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.errorCode").value(0))
                .andExpect(jsonPath("$.data.registration.user.username").value("firebase-user@example.com"))
                .andExpect(jsonPath("$.data.registration.tokenResponse.accessToken").value("access"))
                .andExpect(jsonPath("$.data.registration.tokenResponse.refreshToken").value("refresh"))
                .andExpect(jsonPath("$.data.registration.tokenResponse.expiresIn").value(3600));

        verify(authUserService).getFirebaseSession("USR");
        verify(tokenService).createTokensForUser("firebase-user@example.com", "USR");
    }

    private Jwt buildRefreshJwt() {
        Instant now = Instant.now();
        return Jwt.withTokenValue("refresh-token")
                .header("alg", "RS256")
                .header("typ", "JWT")
                .header("kid", "test-key")
                .subject("refresh-user@example.com")
                .issuer("https://auth.example")
                .issuedAt(now)
                .notBefore(now.minusSeconds(5))
                .expiresAt(now.plusSeconds(300))
                .claim("jti", "refresh-jti")
                .claim("type", "refresh")
                .build();
    }
}


