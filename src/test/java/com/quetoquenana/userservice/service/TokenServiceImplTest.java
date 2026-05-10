package com.quetoquenana.userservice.service;

import com.quetoquenana.userservice.dto.TokenResponse;
import com.quetoquenana.userservice.exception.AuthenticationException;
import com.quetoquenana.userservice.model.RefreshToken;
import com.quetoquenana.userservice.model.User;
import com.quetoquenana.userservice.model.UserStatus;
import com.quetoquenana.userservice.repository.RefreshTokenRepository;
import com.quetoquenana.userservice.service.impl.TokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

class TokenServiceImplTest {

    private static final String APP_CODE = "app1";
    private static final String REFRESH_TOKEN = "dummy";

    JwtEncoder jwtEncoder;
    UserService userService;
    CustomUserDetailsService userDetailsService;
    RefreshTokenRepository refreshTokenRepository;
    TokenServiceImpl tokenService;

    @BeforeEach
    void setup() {
        jwtEncoder = Mockito.mock(JwtEncoder.class);
        userService = Mockito.mock(UserService.class);
        userDetailsService = Mockito.mock(CustomUserDetailsService.class);
        refreshTokenRepository = Mockito.mock(com.quetoquenana.userservice.repository.RefreshTokenRepository.class);

        tokenService = new TokenServiceImpl(
                userService, 
                userDetailsService, 
                jwtEncoder, 
                refreshTokenRepository,
                3600L, 
                86400L, 
                "https://auth.example"
        );
    }

    private RefreshToken buildStoredRefreshToken() {
        return RefreshToken.builder()
                .token(REFRESH_TOKEN)
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();
    }

    private Jwt buildJwt(String tokenValue, Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue(tokenValue);
        // Jwt.Builder requires at least one header entry
        builder.header("alg", "none");
        builder.header("typ", "JWT");
        // set a default issuer for tests; specific tests can construct a Jwt without issuer when needed
        builder.issuer("https://auth.example");
        builder.subject((String) claims.getOrDefault("sub", "user"));
        builder.issuedAt(Instant.now());
        builder.expiresAt(Instant.now().plusSeconds(3600));
        builder.claim("jti", claims.getOrDefault("jti", UUID.randomUUID().toString()));
        claims.forEach(builder::claim);
        return builder.build();
    }

    private Authentication refreshAuthentication(String tokenValue, Map<String, Object> claims) {
        return new JwtAuthenticationToken(buildJwt(tokenValue, claims));
    }

    @Test
    void refresh_shouldThrow_whenMissingSubject() {
        Authentication authentication = refreshAuthentication(REFRESH_TOKEN, Map.of("sub", "", "type", "refresh"));
        when(refreshTokenRepository.findByToken(REFRESH_TOKEN)).thenReturn(Optional.of(buildStoredRefreshToken()));
        assertThrows(AuthenticationException.class, () -> tokenService.refresh(authentication, APP_CODE));
    }

    @Test
    void refresh_shouldThrow_whenAuthenticationIsNotJwtAuthenticationToken() {
        Authentication authentication = Mockito.mock(Authentication.class);
        assertThrows(AuthenticationException.class, () -> tokenService.refresh(authentication, APP_CODE));
    }

    @Test
    void refresh_shouldUseBearerTokenValueFromAuthentication() {
        Authentication authentication = refreshAuthentication(REFRESH_TOKEN, Map.of("sub", "user", "type", "refresh"));
        when(refreshTokenRepository.findByToken(REFRESH_TOKEN)).thenReturn(Optional.of(buildStoredRefreshToken()));

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("user");
        user.setUserStatus(UserStatus.ACTIVE);
        when(userService.findByUsername("user")).thenReturn(Optional.of(user));

        UserDetails ud = new org.springframework.security.core.userdetails.User("user", "pass", Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(userDetailsService.loadUserByUsername("user", APP_CODE)).thenReturn(ud);
        when(jwtEncoder.encode(Mockito.any())).thenReturn(org.springframework.security.oauth2.jwt.Jwt.withTokenValue("t").header("alg","none").header("typ","JWT").claim("x","y").build());

        TokenResponse response = tokenService.refresh(authentication, APP_CODE);
        assertNotNull(response);
    }

    @Test
    void refresh_shouldThrow_whenStoredRefreshTokenIsMissing() {
        Authentication authentication = refreshAuthentication(REFRESH_TOKEN, Map.of("sub", "user", "type", "refresh"));
        when(refreshTokenRepository.findByToken(REFRESH_TOKEN)).thenReturn(Optional.empty());
        assertThrows(AuthenticationException.class, () -> tokenService.refresh(authentication, APP_CODE));
    }

    @Test
    void refresh_happyPath() {
        Authentication authentication = refreshAuthentication(REFRESH_TOKEN, Map.of("sub", "user", "type", "refresh", "jti", "refresh-jti"));
        when(refreshTokenRepository.findByToken(REFRESH_TOKEN)).thenReturn(Optional.of(buildStoredRefreshToken()));

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("user");
        user.setUserStatus(UserStatus.ACTIVE);
        when(userService.findByUsername("user")).thenReturn(Optional.of(user));

        UserDetails ud = new org.springframework.security.core.userdetails.User("user", "pass", Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(userDetailsService.loadUserByUsername("user", APP_CODE)).thenReturn(ud);
        when(jwtEncoder.encode(Mockito.any())).thenReturn(org.springframework.security.oauth2.jwt.Jwt.withTokenValue("t").header("alg","none").header("typ","JWT").claim("x","y").build());

        TokenResponse resp = tokenService.refresh(authentication, APP_CODE);
        assertNotNull(resp.getAccessToken());
        assertNotNull(resp.getRefreshToken());
    }

    @Test
    void refresh_shouldThrow_whenRefreshTokenValueIsBlank() {
        JwtAuthenticationToken authentication = Mockito.mock(JwtAuthenticationToken.class);
        Jwt jwt = Mockito.mock(Jwt.class);
        when(authentication.getToken()).thenReturn(jwt);
        when(jwt.getTokenValue()).thenReturn(" ");

        assertThrows(AuthenticationException.class, () -> tokenService.refresh(authentication, APP_CODE));
    }

    @Test
    void refresh_shouldThrow_whenUserNotActive() {
        Authentication authentication = refreshAuthentication(REFRESH_TOKEN, Map.of("sub", "user", "type", "refresh", "jti", "refresh-jti"));
        when(refreshTokenRepository.findByToken(REFRESH_TOKEN)).thenReturn(Optional.of(buildStoredRefreshToken()));

        User user = new User();
        user.setUsername("user");
        user.setUserStatus(UserStatus.INACTIVE);
        when(userService.findByUsername("user")).thenReturn(Optional.of(user));

        AuthenticationException ex = assertThrows(AuthenticationException.class, () -> tokenService.refresh(authentication, APP_CODE));
        assertTrue(ex.getMessage().contains("user.not.active") || ex.getMessage().contains("authentication"));
    }

    @Test
    void createTokens_shouldNormalizeRolesAndIncludeInClaims() {
        // Prepare authentication with ROLE_ prefix and plain role
        Authentication auth = Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn("user");
        Collection<? extends GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("USER")
        );
        doReturn(authorities).when(auth).getAuthorities();

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("user");
        user.setUserStatus(UserStatus.ACTIVE);
        UserDetails ud = new org.springframework.security.core.userdetails.User("user", "pass", Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(userService.findByUsername("user")).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername("user", APP_CODE)).thenReturn(ud);

        // capture the JwtEncoderParameters passed to encoder
        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        when(jwtEncoder.encode(captor.capture())).thenReturn(Jwt.withTokenValue("t").header("alg","none").header("typ","JWT").claim("x","y").build());

        tokenService.createTokens(auth, APP_CODE);

        // The first encode call is for access token; get captured claims
        JwtEncoderParameters params = captor.getAllValues().getFirst();
        JwtClaimsSet claims = params.getClaims();
        Object rolesObj = claims.getClaim("roles");
        assertNotNull(rolesObj);
        assertInstanceOf(List.class, rolesObj);
        List<?> roles = (List<?>) rolesObj;
        assertTrue(roles.contains("USER"));
    }

    @Test
    void createTokens_shouldPersistRefreshToken() {
        Authentication auth = Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn("user");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("user");
        user.setUserStatus(UserStatus.ACTIVE);

        when(userService.findByUsername("user")).thenReturn(Optional.of(user));

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "user",
                "pass",
                Set.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        when(userDetailsService.loadUserByUsername("user", APP_CODE)).thenReturn(userDetails);

        when(jwtEncoder.encode(Mockito.any()))
                .thenReturn(
                        Jwt.withTokenValue("access-token").header("alg", "none").header("typ", "JWT").claim("x", "y").build(),
                        Jwt.withTokenValue("refresh-token").header("alg", "none").header("typ", "JWT").claim("x", "y").build()
                );

        ArgumentCaptor<RefreshToken> refreshCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        when(refreshTokenRepository.save(refreshCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TokenResponse response = tokenService.createTokens(auth, APP_CODE);

        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        RefreshToken savedRefreshToken = refreshCaptor.getValue();
        assertEquals("refresh-token", savedRefreshToken.getToken());
        assertEquals(APP_CODE, savedRefreshToken.getClientApp());
        assertEquals(user, savedRefreshToken.getUser());
        assertFalse(savedRefreshToken.isRevoked());
    }

    @Test
    void createTokensForUser_shouldEncodePersistAndReturnTokens() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("firebase-user@example.com");
        user.setUserStatus(UserStatus.ACTIVE);

        when(userService.findByUsername("firebase-user@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "firebase-user@example.com",
                "pass",
                Set.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        when(userDetailsService.loadUserByUsername("firebase-user@example.com", APP_CODE)).thenReturn(userDetails);

        ArgumentCaptor<JwtEncoderParameters> jwtCaptor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        when(jwtEncoder.encode(jwtCaptor.capture()))
                .thenReturn(
                        Jwt.withTokenValue("access-token").header("alg", "none").header("typ", "JWT").claim("x", "y").build(),
                        Jwt.withTokenValue("refresh-token").header("alg", "none").header("typ", "JWT").claim("x", "y").build()
                );

        ArgumentCaptor<RefreshToken> refreshCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        when(refreshTokenRepository.save(refreshCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TokenResponse response = tokenService.createTokensForUser("firebase-user@example.com", APP_CODE);

        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(3600L, response.getExpiresIn());

        Mockito.verify(userService).findByUsername("firebase-user@example.com");
        Mockito.verify(userDetailsService).loadUserByUsername("firebase-user@example.com", APP_CODE);
        Mockito.verify(refreshTokenRepository).save(Mockito.any(RefreshToken.class));

        RefreshToken savedRefreshToken = refreshCaptor.getValue();
        assertEquals("refresh-token", savedRefreshToken.getToken());
        assertEquals(APP_CODE, savedRefreshToken.getClientApp());
        assertEquals(user, savedRefreshToken.getUser());
        assertFalse(savedRefreshToken.isRevoked());

        JwtClaimsSet accessClaims = jwtCaptor.getAllValues().get(0).getClaims();
        JwtClaimsSet refreshClaims = jwtCaptor.getAllValues().get(1).getClaims();

        assertEquals("firebase-user@example.com", accessClaims.getSubject());
        assertEquals(List.of(APP_CODE), accessClaims.getAudience());
        assertEquals("auth", accessClaims.getClaim("type"));
        assertEquals(List.of("USER"), accessClaims.getClaim("roles"));

        assertEquals("firebase-user@example.com", refreshClaims.getSubject());
        assertTrue(refreshClaims.getAudience() == null || refreshClaims.getAudience().isEmpty());
        assertEquals("refresh", refreshClaims.getClaim("type"));
        assertNull(refreshClaims.getClaim("roles"));
        assertNotNull(refreshClaims.getIssuedAt());
        assertNotNull(refreshClaims.getNotBefore());
        assertNotNull(refreshClaims.getExpiresAt());
        assertNotNull(refreshClaims.getId());
    }
}
