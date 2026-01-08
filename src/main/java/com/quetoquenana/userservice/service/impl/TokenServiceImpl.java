package com.quetoquenana.userservice.service.impl;

import com.quetoquenana.userservice.dto.TokenResponse;
import com.quetoquenana.userservice.exception.AuthenticationException;
import com.quetoquenana.userservice.model.Application;
import com.quetoquenana.userservice.model.User;
import com.quetoquenana.userservice.model.UserStatus;
import com.quetoquenana.userservice.service.ApplicationService;
import com.quetoquenana.userservice.service.TokenService;
import com.quetoquenana.userservice.service.UserService;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.quetoquenana.userservice.util.Constants.JWTClaims.*;

@Service
public class TokenServiceImpl implements TokenService {

    private final ApplicationService applicationService;
    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final long accessTokenSeconds;
    private final long refreshTokenSeconds;
    private final String issuer;

    public TokenServiceImpl(ApplicationService applicationService, UserService userService, UserDetailsService userDetailsService,
                            JwtEncoder jwtEncoder,
                            JwtDecoder jwtDecoder,
                            @Value("${security.jwt.access-token-seconds:604800}") long accessTokenSeconds,
                            @Value("${security.jwt.refresh-token-seconds:604800}") long refreshTokenSeconds,
                            @Value("${security.jwt.issuer}") String issuer) {
        this.applicationService = applicationService;
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.accessTokenSeconds = accessTokenSeconds;
        this.refreshTokenSeconds = refreshTokenSeconds;
        this.issuer = issuer;
    }

    @Override
    public TokenResponse createTokens(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new AuthenticationException("error.authentication"));

        return getTokenResponse(user, getRoles(authentication.getAuthorities()));
    }

    @Override
    public TokenResponse refresh(String refreshToken) {
        // validate and decode the refresh token
        Jwt jwt = jwtDecoder.decode(refreshToken);

        // validate presence of required claims for refresh tokens
        validateRequiredClaims(jwt);

        if (!TYPE_REFRESH.equals(jwt.getClaimAsString(KEY_TYPE))) {
            throw new AuthenticationException("error.authentication.invalid.refresh.token");
        }

        User user = userService.findByUsername(jwt.getSubject())
                .orElseThrow(() -> new AuthenticationException("error.authentication"));

        if (!user.getUserStatus().equals(UserStatus.ACTIVE)) {
            throw new AuthenticationException("error.authentication.user.not.active");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        return getTokenResponse(user, getRoles(userDetails.getAuthorities()));
    }

    @NonNull
    private TokenResponse getTokenResponse(User user, List<String> roles) {
        Instant now = Instant.now();
        List<String> audienceList = getAudienceList();

        JwtClaimsSet claims = buildCommonClaims(now, user, TYPE_AUTH, accessTokenSeconds, audienceList, roles);
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        JwtClaimsSet refreshClaims = buildCommonClaims(now, user, TYPE_REFRESH, refreshTokenSeconds, audienceList, roles);
        String newRefreshToken = jwtEncoder.encode(JwtEncoderParameters.from(refreshClaims)).getTokenValue();

        return new TokenResponse(accessToken, newRefreshToken, accessTokenSeconds);
    }


    private static List<String> getRoles(Collection<? extends GrantedAuthority> authorities) {
        // Roles normalization
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.startsWith(ROLE_SUFFIX) ? a.substring(5) : a)
                .collect(Collectors.toList());
    }

    private List<String> getAudienceList() {
        // Use active application names as audience entries (aud). Map to a list of strings.
        List<Application> applications = applicationService.findActive();
        return applications.stream()
                .map(Application::getName)
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
                .build();
    }

    /**
     * Ensure required claims are present and non-empty for refresh tokens. Throws AuthenticationException when invalid.
     * @param jwt decoded token
     */
    private void validateRequiredClaims(Jwt jwt) {
        if (jwt == null) {
            throw new AuthenticationException("error.authentication.invalid.refresh.token");
        }

        // issuer
        String iss = jwt.getIssuer() == null ? null : jwt.getIssuer().toString();
        if (iss == null || iss.isBlank()) {
            throw new AuthenticationException("error.authentication.invalid.refresh.token");
        }

        // subject
        String sub = jwt.getSubject();
        if (sub == null || sub.isBlank()) {
            throw new AuthenticationException("error.authentication.invalid.refresh.token");
        }

        // type
        String type = jwt.getClaimAsString(KEY_TYPE);
        if (type == null || type.isBlank()) {
            throw new AuthenticationException("error.authentication.invalid.refresh.token");
        }

        // audience
        List<String> aud = jwt.getAudience();
        if (aud == null || aud.isEmpty()) {
            throw new AuthenticationException("error.authentication.invalid.refresh.token");
        }

        // roles
        List<String> roles = jwt.getClaimAsStringList(KEY_ROLES);
        if (roles == null || roles.isEmpty()) {
            throw new AuthenticationException("error.authentication.invalid.refresh.token");
        }
    }
}
