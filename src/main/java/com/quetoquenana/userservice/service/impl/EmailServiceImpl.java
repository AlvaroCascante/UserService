package com.quetoquenana.userservice.service.impl;

import com.quetoquenana.userservice.dto.UserEmailInfo;
import com.quetoquenana.userservice.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Locale;

@ConditionalOnProperty(name = "gmail.enabled", havingValue = "false", matchIfMissing = true)
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final MessageSource messageSource;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Value("${app.support.email:admin@quetoquenana.com}")
    private String supportEmail;

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

    @Override
    public void sendPasswordEmail(UserEmailInfo user, String plainPassword, Locale locale) {
        Object[] args = new Object[]{user.getPersonName(), user.getPersonLastname(), user.getUsername(), plainPassword, supportEmail};

        String subject = messageSource.getMessage("email.password.reset.subject", null, "User Service - Password reset", locale);
        String textBody = messageSource.getMessage("email.password.reset.text", args, "", locale);

        // render HTML via Thymeleaf template
        Context ctx = new Context(locale);
        ctx.setVariable("name", user.getPersonName());
        ctx.setVariable("lastname", user.getPersonLastname());
        ctx.setVariable("username", user.getUsername());
        ctx.setVariable("password", plainPassword);
        ctx.setVariable("supportEmail", supportEmail);
        String htmlBody = templateEngine.process("password-reset", ctx);

        send(user.getUsername(), subject, textBody, htmlBody);
    }

    @Override
    public void sendNewUserEmail(UserEmailInfo user, String initialPassword, Locale locale) {
        Object[] args = new Object[]{user.getPersonName(), user.getPersonLastname(), user.getUsername(), initialPassword, supportEmail};

        String subject = messageSource.getMessage("email.new.user.subject", null, "Welcome to User Service", locale);
        String textBody = messageSource.getMessage("email.new.user.text", args, "", locale);

        // render HTML via Thymeleaf template
        Context ctx = new Context(locale);
        ctx.setVariable("name", user.getPersonName());
        ctx.setVariable("lastname", user.getPersonLastname());
        ctx.setVariable("username", user.getUsername());
        ctx.setVariable("password", initialPassword);
        ctx.setVariable("supportEmail", supportEmail);
        String htmlBody = templateEngine.process("new-user", ctx);

        send(user.getUsername(), subject, textBody, htmlBody);
    }
}
