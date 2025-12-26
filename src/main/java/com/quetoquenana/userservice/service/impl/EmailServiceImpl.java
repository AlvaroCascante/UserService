package com.quetoquenana.userservice.service.impl;

import com.quetoquenana.userservice.model.User;
import com.quetoquenana.userservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final MessageSource messageSource;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Value("${app.support.email:no-reply@quetoquenana.com}")
    private String supportEmail;

    @Override
    public void sendPasswordEmail(User user, String plainPassword) {
        sendPasswordEmail(user, plainPassword, LocaleContextHolder.getLocale());
    }

    @Override
    public void sendPasswordEmail(User user, String plainPassword, Locale locale) {
        if (user == null || user.getPerson() == null) {
            log.warn("Attempted to send password reset email to invalid user");
            return;
        }

        Object[] args = new Object[]{user.getPerson().getName(), user.getPerson().getLastname(), user.getUsername(), plainPassword, supportEmail};

        String subject = messageSource.getMessage("email.password.reset.subject", null, "User Service - Password reset", locale);
        String textBody = messageSource.getMessage("email.password.reset.text", args, "", locale);

        // render HTML via Thymeleaf template
        Context ctx = new Context(locale);
        ctx.setVariable("name", user.getPerson().getName());
        ctx.setVariable("lastname", user.getPerson().getLastname());
        ctx.setVariable("username", user.getUsername());
        ctx.setVariable("password", plainPassword);
        ctx.setVariable("supportEmail", supportEmail);
        String htmlBody = templateEngine.process("password-reset", ctx);

        send(user.getUsername(), subject, textBody, htmlBody);
    }

    @Override
    public void sendNewUserEmail(User user, String initialPassword) {
        sendNewUserEmail(user, initialPassword, LocaleContextHolder.getLocale());
    }

    @Override
    public void sendNewUserEmail(User user, String initialPassword, Locale locale) {
        if (user == null || user.getPerson() == null) {
            log.warn("Attempted to send new-user email to invalid user");
            return;
        }

        Object[] args = new Object[]{user.getPerson().getName(), user.getPerson().getLastname(), user.getUsername(), initialPassword, supportEmail};

        String subject = messageSource.getMessage("email.new.user.subject", null, "Welcome to User Service", locale);
        String textBody = messageSource.getMessage("email.new.user.text", args, "", locale);

        // render HTML via Thymeleaf template
        Context ctx = new Context(locale);
        ctx.setVariable("name", user.getPerson().getName());
        ctx.setVariable("lastname", user.getPerson().getLastname());
        ctx.setVariable("username", user.getUsername());
        ctx.setVariable("password", initialPassword);
        ctx.setVariable("supportEmail", supportEmail);
        String htmlBody = templateEngine.process("new-user", ctx);

        send(user.getUsername(), subject, textBody, htmlBody);
    }

    private void send(String to, String subject, String textBody, String htmlBody) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setSubject(subject);
            helper.setTo(to);

            String effectiveFrom = (fromAddress != null && fromAddress.contains("@")) ? fromAddress : supportEmail;
            helper.setFrom(effectiveFrom);

            if (supportEmail != null && !supportEmail.isBlank() && !supportEmail.equalsIgnoreCase(effectiveFrom)) {
                helper.setReplyTo(supportEmail);
                try {
                    message.setHeader("Sender", supportEmail);
                } catch (Exception ignored) {}
            }

            helper.setText(textBody, htmlBody);

            javaMailSender.send(message);

            log.info("Email sent to {} (from={}) subject={}", to, effectiveFrom, subject);
        } catch (MessagingException e) {
            log.error("Failed to prepare email for recipient {}: {}", to, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
