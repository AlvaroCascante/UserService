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
}

