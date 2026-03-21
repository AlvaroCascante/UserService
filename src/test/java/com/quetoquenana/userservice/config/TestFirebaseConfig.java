package com.quetoquenana.userservice.config;

import com.quetoquenana.userservice.service.FirebaseTokenVerifier;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "false")
public class TestFirebaseConfig {

    @Bean
    @Primary
    public FirebaseTokenVerifier firebaseTokenVerifier() {
        return Mockito.mock(FirebaseTokenVerifier.class);
    }
}

