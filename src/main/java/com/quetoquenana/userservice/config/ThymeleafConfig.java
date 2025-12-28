package com.quetoquenana.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import static com.quetoquenana.userservice.util.Constants.Emails.*;
import static com.quetoquenana.userservice.util.Constants.MessageSource.DEFAULT;

@Configuration
public class ThymeleafConfig {

    @Bean
    public SpringTemplateEngine springTemplateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.addTemplateResolver(htmlTemplateResolver());
        return engine;
    }

    private ITemplateResolver htmlTemplateResolver() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix(TEMPLATES_PATH);
        resolver.setSuffix(SUFFIX);
        resolver.setTemplateMode(TEMPLATE_MODE);
        resolver.setCharacterEncoding(DEFAULT);
        resolver.setCacheable(false); // disable caching for development
        return resolver;
    }

    //TODO Take care of
    // Template email.password.reset.*, email.new.user.*, and email.new.user.password templates also
    // include the user's initial or temporary password directly in the email body
    // (e.g., Contraseña temporal: {3} / Su contraseña inicial es: {0}), which leaks credentials in
    // cleartext to the email channel. Anyone with access to the mailbox or email transport/storage can
    // obtain the password and hijack the account without going through a secure reset flow.
    // Align these templates with a token-based reset/activation mechanism that avoids embedding the
    // raw password and update the service so it passes only non-sensitive data into these placeholders.
}

