package com.quetoquenana.userservice.service.impl;

import com.quetoquenana.userservice.dto.UserEmailInfo;
import com.quetoquenana.userservice.model.Person;
import com.quetoquenana.userservice.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EmailServiceImplTest {

    private JavaMailSender javaMailSender;
    private MessageSource messageSource;
    private SpringTemplateEngine templateEngine;
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() throws Exception {
        javaMailSender = mock(JavaMailSender.class);
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

        emailService = new EmailServiceImpl(javaMailSender, messageSource, templateEngine);

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
    void sendPasswordResetEmail_sendsMessage() {
        // Arrange
        Session session = Session.getInstance(new Properties());
        MimeMessage mimeMessage = new MimeMessage(session);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        UserEmailInfo user = new UserEmailInfo();
        user.setUsername("alice@example.com");
        user.setPersonName("Alice");
        user.setPersonLastname("Smith");

        String plainPassword = "Temp1234!";

        // Act
        emailService.sendPasswordEmail(user, plainPassword, Locale.ENGLISH);

        // Assert
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordResetEmail_withDtoFromUser_sendsMessage() {
        // Arrange
        Session session = Session.getInstance(new Properties());
        MimeMessage mimeMessage = new MimeMessage(session);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        Person person = new Person();
        person.setName("Alice");
        person.setLastname("Smith");

        User user = new User();
        user.setUsername("alice@example.com");
        user.setPerson(person);

        String plainPassword = "Temp1234!";

        // build DTO (simulate building inside transactional thread)
        UserEmailInfo dto = UserEmailInfo.from(user);

        // Act
        emailService.sendPasswordEmail(dto, plainPassword, Locale.ENGLISH);

        // Assert
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }
}
