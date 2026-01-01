package com.quetoquenana.userservice.config;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import com.google.api.services.gmail.Gmail;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Configuration
@ConditionalOnProperty(name = "gmail.enabled", havingValue = "true")
public class GmailConfig {

    @Value("${GMAIL_CLIENT_ID}")
    private String clientId;
    @Value("${GMAIL_CLIENT_SECRET}")
    private String clientSecret;
    @Value("${GMAIL_REFRESH_TOKEN}")
    private String refreshToken;
    @Value("${GMAIL_APP_NAME:UserService}")
    private String appName;

    @Bean
    public Gmail gmail() throws GeneralSecurityException, IOException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        UserCredentials userCredentials = UserCredentials.newBuilder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(refreshToken)
                .build();

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(userCredentials);

        return new Gmail.Builder(httpTransport, jsonFactory, requestInitializer)
                .setApplicationName(appName)
                .build();
    }
}
