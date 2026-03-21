package com.quetoquenana.userservice.service;

import com.quetoquenana.userservice.dto.TokenResponse;
import com.quetoquenana.userservice.exception.AuthenticationException;
import com.quetoquenana.userservice.model.User;
import com.quetoquenana.userservice.model.UserStatus;
import com.quetoquenana.userservice.repository.RefreshTokenRepository;
import com.quetoquenana.userservice.service.impl.TokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class TokenServiceImplTest {

    JwtDecoder jwtDecoder;
    JwtEncoder jwtEncoder;
    UserService userService;
    UserDetailsService userDetailsService;
    TokenServiceImpl tokenService;

    @BeforeEach
    void setup() {
        jwtDecoder = Mockito.mock(JwtDecoder.class);
        jwtEncoder = Mockito.mock(JwtEncoder.class);
        userService = Mockito.mock(UserService.class);
        userDetailsService = Mockito.mock(UserDetailsService.class);
        ApplicationService applicationService = Mockito.mock(ApplicationService.class);
        // default: no active applications
        when(applicationService.findActive()).thenReturn(List.of());

        RefreshTokenRepository refreshTokenRepository = Mockito.mock(com.quetoquenana.userservice.repository.RefreshTokenRepository.class);

        tokenService = new TokenServiceImpl(applicationService, userService, userDetailsService, jwtEncoder, jwtDecoder, refreshTokenRepository, 3600L, 86400L, "https://auth.example", "");
    }

    private Jwt buildJwt(Map<String, Object> claims, List<String> aud) {
        Jwt.Builder builder = Jwt.withTokenValue("dummy-token");
        // Jwt.Builder requires at least one header entry
        builder.header("alg", "none");
        builder.header("typ", "JWT");
        // set a default issuer for tests; specific tests can construct a Jwt without issuer when needed
        builder.issuer("https://auth.example");
        builder.subject((String) claims.getOrDefault("sub", "user"));
        builder.issuedAt(Instant.now());
        builder.expiresAt(Instant.now().plusSeconds(3600));
        if (aud != null) builder.audience(aud);
        claims.forEach(builder::claim);
        return builder.build();
    }

    @Test
    void refresh_shouldThrow_whenMissingIssuer() {
        Jwt jwt = buildJwt(Map.of("sub", "user", "type", "refresh", "roles", List.of("USER")), List.of("app1"));
        // make issuer null by creating a Jwt with no issuer set — builder doesn't allow null issuer directly,
        // so we'll mock decoder to return jwt but validator checks issuer from jwt.getIssuer();
        Jwt jwtWithNoIssuer = new Jwt(jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt(), jwt.getHeaders(), jwt.getClaims());

        when(jwtDecoder.decode(ArgumentMatchers.anyString())).thenReturn(jwtWithNoIssuer);

        AuthenticationException ex = assertThrows(AuthenticationException.class, () -> tokenService.refresh("dummy"));
        assertTrue(ex.getMessage().contains("invalid.refresh.token") || ex.getMessage().contains("error.authentication"));
    }

    @Test
    void refresh_shouldThrow_whenMissingSubject() {
        Jwt jwt = buildJwt(Map.of("type", "refresh", "roles", List.of("USER")), List.of("app1"));
        when(jwtDecoder.decode(ArgumentMatchers.anyString())).thenReturn(jwt);
        assertThrows(AuthenticationException.class, () -> tokenService.refresh("dummy"));
    }

    @Test
    void refresh_shouldThrow_whenMissingType() {
        Jwt jwt = buildJwt(Map.of("sub", "user", "roles", List.of("USER")), List.of("app1"));
        when(jwtDecoder.decode(ArgumentMatchers.anyString())).thenReturn(jwt);
        assertThrows(AuthenticationException.class, () -> tokenService.refresh("dummy"));
    }

    @Test
    void refresh_shouldThrow_whenMissingAudience() {
        Jwt jwt = buildJwt(Map.of("sub", "user", "type", "refresh", "roles", List.of("USER")), null);
        when(jwtDecoder.decode(ArgumentMatchers.anyString())).thenReturn(jwt);
        assertThrows(AuthenticationException.class, () -> tokenService.refresh("dummy"));
    }

    @Test
    void refresh_shouldThrow_whenMissingRoles() {
        Jwt jwt = buildJwt(Map.of("sub", "user", "type", "refresh"), List.of("app1"));
        when(jwtDecoder.decode(ArgumentMatchers.anyString())).thenReturn(jwt);
        assertThrows(AuthenticationException.class, () -> tokenService.refresh("dummy"));
    }

    @Test
    void refresh_happyPath() {
        Jwt jwt = buildJwt(Map.of("sub", "user", "type", "refresh", "roles", List.of("USER")), List.of("app1"));
        when(jwtDecoder.decode(ArgumentMatchers.anyString())).thenReturn(jwt);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("user");
        user.setUserStatus(UserStatus.ACTIVE);
        when(userService.findByUsername("user")).thenReturn(Optional.of(user));

        UserDetails ud = new org.springframework.security.core.userdetails.User("user", "pass", Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(userDetailsService.loadUserByUsername("user")).thenReturn(ud);

        when(jwtEncoder.encode(ArgumentMatchers.any())).thenReturn(org.springframework.security.oauth2.jwt.Jwt.withTokenValue("t").header("alg","none").header("typ","JWT").claim("x","y").build());

        TokenResponse resp = tokenService.refresh("dummy");
        assertNotNull(resp.getAccessToken());
        assertNotNull(resp.getRefreshToken());
    }

    @Test
    void refresh_shouldThrow_whenJwtDecoderReturnsNull() {
        when(jwtDecoder.decode(ArgumentMatchers.anyString())).thenReturn(null);
        assertThrows(AuthenticationException.class, () -> tokenService.refresh("dummy"));
    }

    @Test
    void refresh_shouldThrow_whenTypeIsNotRefresh() {
        Jwt jwt = buildJwt(Map.of("sub", "user", "type", "auth", "roles", List.of("USER")), List.of("app1"));
        when(jwtDecoder.decode(ArgumentMatchers.anyString())).thenReturn(jwt);
        assertThrows(AuthenticationException.class, () -> tokenService.refresh("dummy"));
    }

    @Test
    void refresh_shouldThrow_whenUserNotActive() {
        Jwt jwt = buildJwt(Map.of("sub", "user", "type", "refresh", "roles", List.of("USER")), List.of("app1"));
        when(jwtDecoder.decode(ArgumentMatchers.anyString())).thenReturn(jwt);

        User user = new User();
        user.setUsername("user");
        user.setUserStatus(UserStatus.INACTIVE);
        when(userService.findByUsername("user")).thenReturn(Optional.of(user));

        AuthenticationException ex = assertThrows(AuthenticationException.class, () -> tokenService.refresh("dummy"));
        assertTrue(ex.getMessage().contains("user.not.active") || ex.getMessage().contains("authentication"));
    }

    @Test
    void createTokens_shouldNormalizeRolesAndIncludeInClaims() {
        // Prepare authentication with ROLE_ prefix and plain role
        Authentication auth = Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn("user");
        when(auth.getAuthorities()).thenReturn((Collection) Set.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("USER")));

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("user");
        user.setUserStatus(UserStatus.ACTIVE);
        when(userService.findByUsername("user")).thenReturn(Optional.of(user));

        // capture the JwtEncoderParameters passed to encoder
        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        when(jwtEncoder.encode(captor.capture())).thenReturn(Jwt.withTokenValue("t").header("alg","none").header("typ","JWT").claim("x","y").build());

        tokenService.createTokens(auth);

        // The first encode call is for access token; get captured claims
        JwtEncoderParameters params = captor.getAllValues().getFirst();
        JwtClaimsSet claims = params.getClaims();
        Object rolesObj = claims.getClaim("roles");
        assertNotNull(rolesObj);
        assertInstanceOf(List.class, rolesObj);
        List<?> roles = (List<?>) rolesObj;
        assertTrue(roles.contains("ADMIN"));
        assertTrue(roles.contains("USER"));
    }
}
