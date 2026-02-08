package com.quetoquenana.userservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Configuration
@Slf4j
public class FirebaseConfig {

    @PostConstruct
    public void initialize() throws IOException {
        // If already initialized, do nothing
        if (FirebaseApp.getApps() != null && !FirebaseApp.getApps().isEmpty()) {
            return;
        }

        String base64 = System.getenv("FIREBASE_CREDENTIALS_BASE64");

        if (base64 == null || base64.isBlank()) {
            throw new IllegalStateException("FIREBASE_CREDENTIALS_BASE64 not set");
        }

        byte[] decoded = Base64.getDecoder().decode(base64);
        InputStream serviceAccount = new ByteArrayInputStream(decoded);

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);
    }
}
