package com.quetoquenana.userservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

@TestConfiguration
public class TestSecurityConfig {

    @Bean(name = "userDetailsService")
    public UserDetailsService userDetailsService() {
        return username -> {
            // provide a minimal user with SYSTEM role so security checks during tests don't fail
            UserDetails ud = User.withUsername(username)
                    .password("{noop}test")
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_SYSTEM")))
                    .build();
            return ud;
        };
    }
}

