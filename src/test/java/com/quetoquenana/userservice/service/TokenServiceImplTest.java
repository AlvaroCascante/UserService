package com.quetoquenana.userservice.service;

import com.quetoquenana.userservice.dto.TokenResponse;
import com.quetoquenana.userservice.exception.AuthenticationException;
import com.quetoquenana.userservice.model.User;
import com.quetoquenana.userservice.model.UserStatus;
import com.quetoquenana.userservice.service.impl.TokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

        tokenService = new TokenServiceImpl(null, userService, userDetailsService, jwtEncoder, jwtDecoder, 3600L, 86400L, "https://auth.example");
    }

    private Jwt buildJwt(Map<String, Object> claims, List<String> aud) {
        Jwt.Builder builder = Jwt.withTokenValue("dummy-token");
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
        user.setUsername("user");
        user.setUserStatus(UserStatus.ACTIVE);
        when(userService.findByUsername("user")).thenReturn(Optional.of(user));

        UserDetails ud = new org.springframework.security.core.userdetails.User("user", "pass", Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(userDetailsService.loadUserByUsername("user")).thenReturn(ud);

        // mock encoder to return something predictable
        when(jwtEncoder.encode(ArgumentMatchers.any())).thenReturn(org.springframework.security.oauth2.jwt.Jwt.withTokenValue("t").claim("x","y").build());

        TokenResponse resp = tokenService.refresh("dummy");
        assertNotNull(resp.getAccessToken());
        assertNotNull(resp.getRefreshToken());
    }
}
