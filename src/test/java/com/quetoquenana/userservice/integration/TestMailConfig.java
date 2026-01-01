package com.quetoquenana.userservice.integration;

import com.google.api.services.gmail.Gmail;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;

@TestConfiguration
public class TestMailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        // provide a Mockito mock so integration tests don't require a real SMTP server
        return Mockito.mock(JavaMailSender.class);
    }

    @Bean
    public Gmail gmail() {
        // provide a Mockito mock for the Gmail API client so EmailServiceImpl can be autowired in tests
        return Mockito.mock(Gmail.class);
    }
}
