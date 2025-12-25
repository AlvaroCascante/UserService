package com.quetoquenana.userservice.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;

@Component
public class BeanInspector {

    private static final Logger log = LoggerFactory.getLogger(BeanInspector.class);

    private final ApplicationContext ctx;
    private final Environment env;

    public BeanInspector(ApplicationContext ctx, Environment env) {
        this.ctx = ctx;
        this.env = env;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void onStarted(ApplicationStartedEvent e) {
        log.info("[BeanInspector] application started");
        inspect();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady(ApplicationReadyEvent e) {
        log.info("[BeanInspector] application ready");
        inspect();
    }

    private void inspect() {
        String[] all = ctx.getBeanDefinitionNames();
        log.info("[BeanInspector] total beans = {}", all.length);

        String[] converterBeans = ctx.getBeanNamesForType(Converter.class);
        log.info("[BeanInspector] Converter beans ({}) : {}", converterBeans.length,
                Arrays.stream(converterBeans).sorted().collect(Collectors.joining(", ")));

        String cpbBeans = Arrays.stream(all)
                .filter(name -> {
                    Class<?> type = ctx.getType(name);
                    return type != null && type.isAnnotationPresent(org.springframework.boot.context.properties.ConfigurationPropertiesBinding.class);
                })
                .sorted()
                .collect(Collectors.joining(", "));
        log.info("[BeanInspector] @ConfigurationPropertiesBinding beans: {}", cpbBeans);

        Arrays.stream(new String[]{"rsaKeyConverter", "rsaPublicKeyConverter", "rsaPrivateKeyConverter"})
                .forEach(n -> log.info("[BeanInspector] containsBean('{}') = {}", n, ctx.containsBean(n)));

        String raw = env.getProperty("security.rsa.public-key");
        if (raw == null) {
            log.info("[BeanInspector] security.rsa.public-key = <null>");
        } else {
            String clean = raw.replace("\n", "\\n");
            int len = clean.length();
            String head = clean.substring(0, Math.min(40, len));
            String tail = len > 40 ? clean.substring(Math.max(0, len - 40)) : "";
            log.info("[BeanInspector] security.rsa.public-key length={} head=\"{}\" tail=\"{}\"", len, head, tail);

            String maybeBase64 = clean;
            if (maybeBase64.contains("-----BEGIN")) {
                maybeBase64 = maybeBase64.replaceAll("(?s)-----BEGIN[^-]+-----", "")
                        .replaceAll("(?s)-----END[^-]+-----", "")
                        .replaceAll("\\\\n", "")
                        .replaceAll("\\s", "");
            }
            try {
                Base64.getDecoder().decode(maybeBase64);
                log.info("[BeanInspector] base64 decode: OK");
            } catch (IllegalArgumentException ex) {
                log.info("[BeanInspector] base64 decode: FAILED -> {}", ex.getMessage());
            }
        }
    }
}