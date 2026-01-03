package com.quetoquenana.userservice.config;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.quetoquenana.userservice.exception.AuthenticationException;
import com.quetoquenana.userservice.model.AppRoleUser;
import com.quetoquenana.userservice.model.Application;
import com.quetoquenana.userservice.repository.AppRoleUserRepository;
import com.quetoquenana.userservice.repository.ApplicationRepository;
import com.quetoquenana.userservice.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.quetoquenana.userservice.util.Constants.Headers.APP_NAME;
import static com.quetoquenana.userservice.util.Constants.OAuth2.TOKEN_CLAIM_ROLES;
import static com.quetoquenana.userservice.util.Constants.OAuth2.TOKEN_CLAIM_SUB;
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

    private final ApplicationRepository applicationRepository;
    private final AppRoleUserRepository appRoleUserRepository;
    private final UserRepository userRepository;

    @Bean
    public SecurityFilterChain basicAuthChain(HttpSecurity http) throws Exception {
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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // disable for API clients; enable if using browser forms
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/util/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api/auth/forgot-password", "/api/auth/refresh").permitAll()
                        .anyRequest().authenticated()
                )
                // enable basic auth so Spring decodes credentials automatically
                .httpBasic(withDefaults())
                // ensure we use JWT Bearer auth for protected endpoints
                .oauth2ResourceServer(o -> o.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        try {
            log.debug("Creating JwtDecoder using public key configured as: {}", rsaKeyProperties.publicKey());
            return NimbusJwtDecoder.withPublicKey(rsaKeyProperties.publicKey()).build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create JwtDecoder. Check security.rsa.public-key property and format of the public key.", e);
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName(TOKEN_CLAIM_ROLES);
        // we will prefix authorities with ROLE_ so that hasRole('X') checks work
        authoritiesConverter.setAuthorityPrefix(ROLE_PREFIX);

        JwtAuthenticationConverter jwtAuthConverter = new JwtAuthenticationConverter();
        jwtAuthConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        jwtAuthConverter.setPrincipalClaimName(TOKEN_CLAIM_SUB);
        return jwtAuthConverter;
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

    private List<String> splitToList(String value) {
        if (value == null || value.isBlank()) return List.of();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @Bean
    JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

}
