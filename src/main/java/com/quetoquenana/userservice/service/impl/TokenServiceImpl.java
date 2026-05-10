package com.quetoquenana.userservice.service.impl;

import com.quetoquenana.userservice.dto.TokenResponse;
import com.quetoquenana.userservice.exception.AuthenticationException;
import com.quetoquenana.userservice.model.RefreshToken;
import com.quetoquenana.userservice.model.User;
import com.quetoquenana.userservice.model.UserStatus;
import com.quetoquenana.userservice.repository.RefreshTokenRepository;
import com.quetoquenana.userservice.service.CustomUserDetailsService;
import com.quetoquenana.userservice.service.TokenService;
import com.quetoquenana.userservice.service.UserService;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.quetoquenana.userservice.util.Constants.JWTClaims.*;

@Service
public class TokenServiceImpl implements TokenService {

    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;
    private final JwtEncoder jwtEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final long accessTokenSeconds;
    private final long refreshTokenSeconds;
    private final String issuer;

    public TokenServiceImpl(
            UserService userService,
            CustomUserDetailsService userDetailsService,
            JwtEncoder jwtEncoder,
            RefreshTokenRepository refreshTokenRepository,
            @Value("${security.jwt.access-token-seconds:60}") long accessTokenSeconds,
            @Value("${security.jwt.refresh-token-seconds:604800}") long refreshTokenSeconds,
            @Value("${security.jwt.issuer}") String issuer
    ) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.jwtEncoder = jwtEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.accessTokenSeconds = accessTokenSeconds;
        this.refreshTokenSeconds = refreshTokenSeconds;
        this.issuer = issuer;
    }

    @Override
    public TokenResponse createTokens(Authentication authentication, String appCode) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new AuthenticationException("error.authentication"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername(), appCode);

        return getTokenResponse(user, appCode, getRoles(userDetails.getAuthorities()));
    }

    @NonNull
    private TokenResponse getTokenResponse(User user, String appCode, List<String> roles) {
        Instant now = Instant.now();
        List<String> audienceList = List.of(appCode);

        JwtClaimsSet claims = buildCommonClaims(now, user, TYPE_AUTH, accessTokenSeconds, audienceList, roles);
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        JwtClaimsSet refreshClaims = buildCommonClaims(now, user, TYPE_REFRESH, refreshTokenSeconds, audienceList, roles);
        String newRefreshToken = jwtEncoder.encode(JwtEncoderParameters.from(refreshClaims)).getTokenValue();

        persistRefreshToken(user, appCode, newRefreshToken, now);

        return new TokenResponse(accessToken, newRefreshToken, accessTokenSeconds);
    }

    @Override
    public TokenResponse refresh(Authentication authentication, String appCode) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken)) {
            throw new AuthenticationException("error.authentication.invalid.refresh.token");
        }

        String refreshToken = jwtAuthenticationToken.getToken().getTokenValue();
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthenticationException("error.authentication.invalid.refresh.token");
        }

        // first check stored refresh token
        Optional<RefreshToken> stored = refreshTokenRepository.findByToken(refreshToken);
        if (stored.isEmpty() || stored.get().isRevoked() || stored.get().getExpiresAt() == null || stored.get().getExpiresAt().isBefore(Instant.now())) {
            throw new AuthenticationException("error.authentication.invalid.refresh.token");
        }

        String subject = jwtAuthenticationToken.getName();
        if (subject == null || subject.isBlank()) {
            throw new AuthenticationException("error.authentication.invalid.refresh.token");
        }

        User user = userService.findByUsername(subject)
                .orElseThrow(() -> new AuthenticationException("error.authentication"));

        if (!user.getUserStatus().equals(UserStatus.ACTIVE)) {
            throw new AuthenticationException("error.authentication.user.not.active");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername(), appCode);

        return getTokenResponse(user, appCode, getRoles(userDetails.getAuthorities()));
    }

    @Override
    public TokenResponse createTokensForUser(String username, String appCode) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("error.authentication"));
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername(), appCode);
        return getTokenResponse(user, appCode, getRoles(userDetails.getAuthorities()));
    }

    private void persistRefreshToken(User user, String appCode, String refreshToken, Instant issuedAt) {
        RefreshToken rt = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .clientApp(appCode)
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(refreshTokenSeconds, ChronoUnit.SECONDS))
                .revoked(false)
                .build();
        refreshTokenRepository.save(rt);
    }

    private static List<String> getRoles(Collection<? extends GrantedAuthority> authorities) {
        // Roles normalization
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.startsWith(ROLE_SUFFIX) ? a.substring(5) : a)
                .collect(Collectors.toList());
    }

    private JwtClaimsSet buildCommonClaims(
            Instant now,
            User user,
            String type,
            long tokenSeconds,
            List<String> audienceList,
            List<String> roles
    ) {
        if (TYPE_AUTH.equals(type)) {
            return JwtClaimsSet.builder()
                    .subject(user.getUsername())
                    .issuer(issuer)
                    .audience(audienceList)
                    .issuedAt(now)
                    .notBefore(now)
                    .expiresAt(now.plus(tokenSeconds, ChronoUnit.SECONDS))
                    .id(UUID.randomUUID().toString())
                    .claim(KEY_NAME, user.getFullName())
                    .claim(KEY_ROLES, roles)
                    .claim(KEY_TYPE, type)
                    .claim(KEY_USER_ID, user.getId().toString())
                    .build();
        }
        return JwtClaimsSet.builder()
                .subject(user.getUsername())
                .issuer(issuer)
                .issuedAt(now)
                .notBefore(now)
                .expiresAt(now.plus(tokenSeconds, ChronoUnit.SECONDS))
                .id(UUID.randomUUID().toString())
                .claim(KEY_TYPE, type)
                .build();
    }
}
