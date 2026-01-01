// java
package com.quetoquenana.userservice.service.impl;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.quetoquenana.userservice.dto.UserEmailInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EmailServiceImplTest {

    private Gmail gmail;
    private Gmail.Users gmailUsers;
    private Gmail.Users.Messages gmailUsersMessages;
    private Gmail.Users.Messages.Send gmailSend;
    private MessageSource messageSource;
    private SpringTemplateEngine templateEngine;
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() throws Exception {
        // Mock Gmail API chain
        gmail = mock(Gmail.class);
        gmailUsers = mock(Gmail.Users.class);
        gmailUsersMessages = mock(Gmail.Users.Messages.class);
        gmailSend = mock(Gmail.Users.Messages.Send.class);

        when(gmail.users()).thenReturn(gmailUsers);
        when(gmailUsers.messages()).thenReturn(gmailUsersMessages);
        when(gmailUsersMessages.send(eq("me"), any(Message.class))).thenReturn(gmailSend);
        when(gmailSend.execute()).thenReturn(new Message());

        messageSource = mock(MessageSource.class);

        // configure a lightweight template engine that loads templates from classpath templates/email
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/email/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);
        templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(resolver);

        emailService = new EmailServiceImpl(gmail, templateEngine, messageSource);

        // ensure there's a support email fallback (since @Value is not processed outside Spring)
        Field supportField = EmailServiceImpl.class.getDeclaredField("supportEmail");
        supportField.setAccessible(true);
        supportField.set(emailService, "support@example.com");

        // Stub messageSource to return simple bodies for any email keys
        when(messageSource.getMessage(anyString(), any(), anyString(), any(Locale.class))).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            if (key.contains("subject")) return "Subject";
            return "Plain text";
        });
    }

    @Test
    void sendPasswordResetEmail_sendsMessage() throws IOException {
        // Arrange
        UserEmailInfo user = UserEmailInfo.builder()
                .personLastname("Doe")
                .personName("John")
                .username("test")
                .build();

        String plainPassword = "Temp1234!";

        // Act
        emailService.sendPasswordEmail(user, plainPassword, Locale.ENGLISH);

        // Assert - verify Gmail messages.send was called
        verify(gmailUsersMessages, times(1)).send(eq("me"), any(Message.class));
    }

    @Test
    void sendPasswordResetEmail_withDtoFromUser_sendsMessage() throws IOException {

        String plainPassword = "Temp1234!";

        // build DTO (simulate building inside transactional thread)
        UserEmailInfo dto = UserEmailInfo.builder()
                .personLastname("Doe")
                .personName("John")
                .username("test")
                .build();

        // Act
        emailService.sendPasswordEmail(dto, plainPassword, Locale.ENGLISH);

        // Assert
        verify(gmailUsersMessages, times(1)).send(eq("me"), any(Message.class));
    }
}
