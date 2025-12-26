package com.quetoquenana.userservice.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "MAIL_HOST", matches = ".+")
public class MailtrapIntegrationTest {

    @Autowired
    private JavaMailSender javaMailSender;

    @Test
    void sendTestEmail() throws Exception {
        Session session = Session.getInstance(System.getProperties());
        MimeMessage message = new MimeMessage(session);
        message.setRecipient(Message.RecipientType.TO, new InternetAddress("test@example.com"));
        message.setFrom(new InternetAddress("support@example.com"));
        message.setSubject("Integration test");
        message.setText("This is a test");

        javaMailSender.send(message);
        // If no exception thrown, success
        assertTrue(true);
    }
}
