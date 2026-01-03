package com.quetoquenana.userservice.service.impl;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.quetoquenana.userservice.dto.UserEmailInfo;
import com.quetoquenana.userservice.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final Gmail gmail;
    private final SpringTemplateEngine templateEngine;
    private final MessageSource messageSource;

    @Value("${GMAIL_SENDER_EMAIL:}")
    private String fromAddress;

    @Value("${app.support.email:admin@quetoquenana.com}")
    private String supportEmail;

    @Override
    public void sendPasswordEmail(UserEmailInfo user, String plainPassword, Locale locale) {
        Object[] args = new Object[]{user.getPersonName(), user.getPersonLastname(), user.getUsername(), plainPassword, supportEmail};

        String subject = messageSource.getMessage("email.password.reset.subject", null, "User Service - Password reset", locale);
        String textBody = messageSource.getMessage("email.password.reset.text", args, "", locale);

        Context ctx = getContext(user, plainPassword, locale);
        String htmlBody = templateEngine.process("password-reset", ctx);

        sendViaGmail(user.getUsername(), subject, textBody, htmlBody);
    }

    private Context getContext(UserEmailInfo user, String plainPassword, Locale locale) {
        Context ctx = new Context(locale);
        ctx.setVariable("name", user.getPersonName());
        ctx.setVariable("lastname", user.getPersonLastname());
        ctx.setVariable("username", user.getUsername());
        ctx.setVariable("password", plainPassword);
        ctx.setVariable("supportEmail", supportEmail);
        return ctx;
    }

    @Override
    public void sendNewUserEmail(UserEmailInfo user, String initialPassword, Locale locale) {
        Object[] args = new Object[]{user.getPersonName(), user.getPersonLastname(), user.getUsername(), initialPassword, supportEmail};

        String subject = messageSource.getMessage("email.new.user.subject", null, "Welcome to User Service", locale);
        String textBody = messageSource.getMessage("email.new.user.text", args, "", locale);

        Context ctx = getContext(user, initialPassword, locale);
        String htmlBody = templateEngine.process("new-user", ctx);

        sendViaGmail(user.getUsername(), subject, textBody, htmlBody);
    }

    private void sendViaGmail(String to, String subject, String textBody, String htmlBody) {
        try {
            // Build a MimeMessage with both plain text and HTML parts (same as EmailServiceImpl)
            Session session = Session.getInstance(new Properties());
            MimeMessage mimeMessage = new MimeMessage(session);
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            helper.setSubject(subject);
            helper.setTo(to);

            String effectiveFrom = (fromAddress != null && fromAddress.contains("@")) ? fromAddress : supportEmail;
            helper.setFrom(effectiveFrom);

            if (supportEmail != null && !supportEmail.isBlank() && !supportEmail.equalsIgnoreCase(effectiveFrom)) {
                helper.setReplyTo(supportEmail);
                try {
                    mimeMessage.setHeader("Sender", supportEmail);
                } catch (Exception ignored) {}
            }

            helper.setText(textBody, htmlBody);

            // Encode message
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            mimeMessage.writeTo(buffer);
            String raw = Base64.getUrlEncoder().encodeToString(buffer.toByteArray());

            Message msg = new Message();
            msg.setRaw(raw);

            gmail.users().messages().send("me", msg).execute();

            log.info("Email sent to {} (from={}) subject={}", to, effectiveFrom, subject);
        } catch (MessagingException e) {
            log.error("Failed to prepare email for recipient {}: {}", to, e.getMessage(), e);
        } catch (IOException e) {
            log.error("IO error while preparing email for {}: {}", to, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
        }
    }
}
