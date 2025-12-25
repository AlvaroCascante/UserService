package com.quetoquenana.userservice.config;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.quetoquenana.userservice.exception.AuthenticationException;
import com.quetoquenana.userservice.model.AppRoleUser;
import com.quetoquenana.userservice.model.Application;
import com.quetoquenana.userservice.properties.CorsConfigProperties;
import com.quetoquenana.userservice.properties.RsaKeyProperties;
import com.quetoquenana.userservice.repository.AppRoleUserRepository;
import com.quetoquenana.userservice.repository.ApplicationRepository;
import com.quetoquenana.userservice.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import static com.quetoquenana.userservice.util.Constants.Headers.*;
import static com.quetoquenana.userservice.util.Constants.Methods.*;
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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // disable for API clients; enable if using browser forms
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/util/**").permitAll()
                        // allow unauthenticated access to auth endpoints (login/refresh)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
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
    JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // The application name should be supplied by the client in a header (e.g. X-Application-Name).
        // We read it from the current request using RequestContextHolder so we can determine the
        // user's roles for that specific application during username/password authentication.
        return username ->
                userRepository.findByUsernameIgnoreCase(username)
                        .map(user -> {
                            // try to read application name from current request
                            String appName = getAppName();

                            Application application = applicationRepository.findByName(appName)
                                    .orElseThrow(() -> new AuthenticationException("error.authentication.application"));

                            List<AppRoleUser> appRoleUsers = appRoleUserRepository.findByUserIdAndRoleApplicationId(user.getId(), application.getId());
                            List<GrantedAuthority> authorities =  appRoleUsers.stream()
                                    .map(mapping -> new SimpleGrantedAuthority(mapping.getRole().getRoleName()))
                                    .collect(Collectors.toList());

                            return org.springframework.security.core.userdetails.User
                                    .withUsername(user.getUsername())
                                    .password(user.getPasswordHash())
                                    .authorities(authorities)
                                    .accountLocked(user.accountLocked())
                                    .build();
                        })
                        .orElseThrow(() -> new AuthenticationException("error.authentication"));
    }

    private static String getAppName() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String appName = null;
        if (attrs != null) {
            HttpServletRequest req = attrs.getRequest();
            appName = req.getHeader(APP_NAME);
        }

        // if application not provided, fail early (caller can change to allow default behavior)
        if (appName == null || appName.isBlank()) {
            throw new AuthenticationException("error.authentication.application.header");
        }
        return appName;
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

        // Safely read CSV config values and provide sensible defaults when missing
        String hostsCsv = corsConfigProperties == null ? null : corsConfigProperties.getHosts();
        String methodsCsv = corsConfigProperties == null ? null : corsConfigProperties.getMethods();
        String headersCsv = corsConfigProperties == null ? null : corsConfigProperties.getHeaders();

        List<String> allowedOrigins;
        if (hostsCsv == null || hostsCsv.isBlank()) {
            // default to allowing any origin if not configured
            allowedOrigins = List.of("*");
        } else {
            allowedOrigins = Arrays.stream(hostsCsv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }

        List<String> allowedMethods;
        if (methodsCsv == null || methodsCsv.isBlank()) {
            allowedMethods = List.of(GET, POST, PUT, DELETE);
        } else {
            allowedMethods = Arrays.stream(methodsCsv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }

        List<String> allowedHeaders;
        if (headersCsv == null || headersCsv.isBlank()) {
            allowedHeaders = List.of(APP_NAME, CONTENT_TYPE, AUTHORIZATION);
        } else {
            allowedHeaders = Arrays.stream(headersCsv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }

        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(allowedMethods);
        configuration.setAllowedHeaders(allowedHeaders);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
