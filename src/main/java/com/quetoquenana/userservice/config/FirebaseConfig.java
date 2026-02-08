package com.quetoquenana.userservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {

            String credentialsPath = System.getenv("FIREBASE_CREDENTIALS_PATH");

            if (credentialsPath == null || credentialsPath.isBlank()) {
                throw new IllegalStateException(
                        "FIREBASE_CREDENTIALS_PATH environment variable not set"
                );
            }

            try (InputStream serviceAccount =
                         new FileInputStream(credentialsPath)) {

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
            }
        }
    }
}

