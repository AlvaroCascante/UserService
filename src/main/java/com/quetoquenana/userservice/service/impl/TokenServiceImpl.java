package com.quetoquenana.userservice.service.impl;

import com.quetoquenana.userservice.dto.TokenResponse;
import com.quetoquenana.userservice.service.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TokenServiceImpl implements TokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final long accessTokenSeconds;
    private final long refreshTokenSeconds;

    public TokenServiceImpl(JwtEncoder jwtEncoder,
                            JwtDecoder jwtDecoder,
                            @Value("${security.jwt.access-token-seconds:3600}") long accessTokenSeconds,
                            @Value("${security.jwt.refresh-token-seconds:604800}") long refreshTokenSeconds) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.accessTokenSeconds = accessTokenSeconds;
        this.refreshTokenSeconds = refreshTokenSeconds;
    }

    @Override
    public TokenResponse createTokens(Authentication authentication) {
        String username = authentication.getName();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Instant now = Instant.now();

        JwtClaimsSet accessClaims = JwtClaimsSet.builder()
                .subject(username)
                .issuedAt(now)
                .expiresAt(now.plus(accessTokenSeconds, ChronoUnit.SECONDS))
                .claim("roles", roles)
                .build();

        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(accessClaims)).getTokenValue();

        JwtClaimsSet refreshClaims = JwtClaimsSet.builder()
                .subject(username)
                .issuedAt(now)
                .expiresAt(now.plus(refreshTokenSeconds, ChronoUnit.SECONDS))
                .claim("type", "refresh")
                .build();

        String refreshToken = jwtEncoder.encode(JwtEncoderParameters.from(refreshClaims)).getTokenValue();

        return new TokenResponse(accessToken, refreshToken, accessTokenSeconds);
    }

    @Override
    public TokenResponse refresh(String refreshToken) {
        // validate and decode the refresh token
        Jwt jwt = jwtDecoder.decode(refreshToken);
        // check claim 'type' if present
        Object type = jwt.getClaim("type");
        if (type == null || !"refresh".equals(type.toString())) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String username = jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles == null) {
            roles = List.of();
        }

        Instant now = Instant.now();

        JwtClaimsSet accessClaims = JwtClaimsSet.builder()
                .subject(username)
                .issuedAt(now)
                .expiresAt(now.plus(accessTokenSeconds, ChronoUnit.SECONDS))
                .claim("roles", roles)
                .build();

        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(accessClaims)).getTokenValue();

        // Optionally rotate refresh token: create a new one
        JwtClaimsSet newRefreshClaims = JwtClaimsSet.builder()
                .subject(username)
                .issuedAt(now)
                .expiresAt(now.plus(refreshTokenSeconds, ChronoUnit.SECONDS))
                .claim("type", "refresh")
                .build();

        String newRefreshToken = jwtEncoder.encode(JwtEncoderParameters.from(newRefreshClaims)).getTokenValue();

        return new TokenResponse(accessToken, newRefreshToken, accessTokenSeconds);
    }
}
