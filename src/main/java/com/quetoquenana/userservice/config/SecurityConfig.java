package com.quetoquenana.userservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quetoquenana.userservice.dto.ApiResponse;
import com.quetoquenana.userservice.exception.ExpiredTokenException;
import com.quetoquenana.userservice.util.JwtExceptionClassifier;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.quetoquenana.userservice.util.Constants.JWTClaims.KEY_ROLES;
import static com.quetoquenana.userservice.util.Constants.JWTClaims.KEY_SUB;
import static com.quetoquenana.userservice.util.Constants.JWTClaims.KEY_TYPE;
import static com.quetoquenana.userservice.util.Constants.JWTClaims.TYPE_REFRESH;
import static com.quetoquenana.userservice.util.Constants.Roles.ROLE_PREFIX;
import static org.springframework.security.config.Customizer.withDefaults;

@EnableMethodSecurity
@Configuration
@EnableConfigurationProperties({RsaKeyProperties.class, CorsConfigProperties.class})
@AllArgsConstructor
@Slf4j
public class SecurityConfig {

    private final CorsConfigProperties corsConfigProperties;
    private final RsaKeyProperties rsaKeyProperties;


    @Bean
    @Order(1)
    SecurityFilterChain basicAuthChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/auth/login", "/api/auth/reset")
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain firebaseAuthChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/auth/firebase-login", "/api/auth/firebase-registration")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Order(3)
    SecurityFilterChain refreshTokenChain(
            HttpSecurity http,
            AuthenticationEntryPoint authenticationEntryPoint,
            @Qualifier("refreshJwtDecoder") JwtDecoder refreshJwtDecoder
    ) throws Exception {
        http
                .securityMatcher("/api/auth/refresh")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
                .oauth2ResourceServer(o -> o
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .jwt(jwt -> jwt
                                .decoder(refreshJwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    @Bean
    @Order(4)
    SecurityFilterChain filterChain(HttpSecurity http, AuthenticationEntryPoint authenticationEntryPoint) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // disable for API clients; enable if using browser forms
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/util/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers(
                            "/api/auth/forgot-password",
                            "/api/auth/firebase-login",
                            "/api/auth/firebase-registration").permitAll()
                    .requestMatchers("/.well-known/jwks.json").permitAll()
                    .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
            .httpBasic(withDefaults())
            .oauth2ResourceServer(o -> o
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper, MessageSource messageSource) {
        return (request, response, authException) -> {
            ApiResponse apiResponse;
            if (JwtExceptionClassifier.isExpired(authException)) {
                String message = messageSource.getMessage(
                        ExpiredTokenException.MESSAGE_KEY,
                        null,
                        request.getLocale()
                );
                apiResponse = new ApiResponse(message, ExpiredTokenException.ERROR_CODE);
            } else {
                apiResponse = new ApiResponse("Unauthorized", HttpStatus.UNAUTHORIZED.value());
            }

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            writeResponse(objectMapper, response, apiResponse);
        };
    }

    @Bean
    @Primary
    JwtDecoder jwtDecoder(
            @Value("${security.jwt.issuer}") String expectedIssuer,
            @Value("${security.jwt.aud}") String expectedAudience
    ) {
        try {
            String expectedKid = rsaKeyProperties.currentKeyId();

            // Validate issuer (iss) using default validator, and optionally validate audience if configured
            OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(expectedIssuer);
            OAuth2TokenValidator<Jwt> audienceValidator = audienceValidator(expectedAudience);
            OAuth2TokenValidator<Jwt> timestampValidator = new JwtTimestampValidator();
            OAuth2TokenValidator<Jwt> kidValidator = kidValidator(expectedKid);

            DelegatingOAuth2TokenValidator<Jwt> combined = new DelegatingOAuth2TokenValidator<>(
                    issuerValidator,
                    audienceValidator,
                    timestampValidator,
                    kidValidator
            );
            return buildDecoder(combined);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create JwtDecoder. Check security.rsa.public-key property and format of the public key.", e);
        }
    }

    @Bean("refreshJwtDecoder")
    JwtDecoder refreshJwtDecoder(
            @Value("${security.jwt.issuer}") String expectedIssuer
    ) {
        try {
            String expectedKid = rsaKeyProperties.currentKeyId();

            OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(expectedIssuer);
            OAuth2TokenValidator<Jwt> timestampValidator = new JwtTimestampValidator();
            OAuth2TokenValidator<Jwt> kidValidator = kidValidator(expectedKid);
            OAuth2TokenValidator<Jwt> refreshTypeValidator = token -> TYPE_REFRESH.equals(token.getClaimAsString(KEY_TYPE))
                    ? OAuth2TokenValidatorResult.success()
                    : invalidToken("Refresh token type is invalid.");
            OAuth2TokenValidator<Jwt> subjectValidator = token -> hasText(token.getSubject())
                    ? OAuth2TokenValidatorResult.success()
                    : invalidToken("Refresh token subject is missing.");
            OAuth2TokenValidator<Jwt> jwtIdValidator = token -> hasText(token.getId())
                    ? OAuth2TokenValidatorResult.success()
                    : invalidToken("Refresh token jti is missing.");

            DelegatingOAuth2TokenValidator<Jwt> combined = new DelegatingOAuth2TokenValidator<>(
                    issuerValidator,
                    timestampValidator,
                    kidValidator,
                    refreshTypeValidator,
                    subjectValidator,
                    jwtIdValidator
            );
            return buildDecoder(combined);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create refresh JwtDecoder. Check security.rsa.public-key property and format of the public key.", e);
        }
    }


    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Read CORS properties defensively — tests or environments may not provide values.
        List<String> allowedOrigins = splitToList(corsConfigProperties.getHosts());
        List<String> allowedMethods = splitToList(corsConfigProperties.getMethods());
        List<String> allowedHeaders = splitToList(corsConfigProperties.getHeaders());

        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(allowedMethods);
        configuration.setAllowedHeaders(allowedHeaders);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    private OAuth2TokenValidator<Jwt> audienceValidator(String audience) {
        return new JwtClaimValidator<List<String>>(
                OAuth2TokenIntrospectionClaimNames.AUD,
                aud -> aud.contains(audience)
        );
    }

    private OAuth2TokenValidator<Jwt> kidValidator(String expectedKid) {
        if (expectedKid == null || expectedKid.isBlank()) {
            return token -> {
                Objects.requireNonNull(token);
                return OAuth2TokenValidatorResult.success();
            };
        }

        return token -> {
            Object kidHeader = token.getHeaders().get("kid");
            if (expectedKid.equals(kidHeader)) {
                return OAuth2TokenValidatorResult.success();
            }

            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_TOKEN,
                    "JWT kid header does not match the configured key id.",
                    null
            ));
        };
    }

    private JwtDecoder buildDecoder(OAuth2TokenValidator<Jwt> jwtValidator) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(rsaKeyProperties.publicKey()).build();
        decoder.setJwtValidator(jwtValidator);
        return decoder;
    }

    private OAuth2TokenValidatorResult invalidToken(String description) {
        return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                OAuth2ErrorCodes.INVALID_TOKEN,
                description,
                null
        ));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void writeResponse(ObjectMapper objectMapper, jakarta.servlet.http.HttpServletResponse response, ApiResponse apiResponse) throws IOException {
        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName(KEY_ROLES);
        // we will prefix authorities with ROLE_ so that hasRole('X') checks work
        authoritiesConverter.setAuthorityPrefix(ROLE_PREFIX);

        JwtAuthenticationConverter jwtAuthConverter = new JwtAuthenticationConverter();
        jwtAuthConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        jwtAuthConverter.setPrincipalClaimName(KEY_SUB);
        return jwtAuthConverter;
    }

    private List<String> splitToList(String value) {
        if (value == null || value.isBlank()) return List.of();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

}
