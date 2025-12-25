package com.quetoquenana.userservice.config;


import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;

@Component
public class BeanInspector implements ApplicationListener<ApplicationReadyEvent> {

    private final ApplicationContext ctx;
    private final Environment env;

    public BeanInspector(ApplicationContext ctx, Environment env) {
        this.ctx = ctx;
        this.env = env;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String[] all = ctx.getBeanDefinitionNames();
        System.out.println("[BeanInspector] total beans = " + all.length);

        // list Converter beans
        String[] converterBeans = ctx.getBeanNamesForType(Converter.class);
        System.out.println("[BeanInspector] Converter beans (" + converterBeans.length + "): "
                + Arrays.stream(converterBeans).sorted().collect(Collectors.joining(", ")));

        // list beans whose class is annotated with @ConfigurationPropertiesBinding
        String cpbBeans = Arrays.stream(all)
                .filter(name -> {
                    Class<?> type = ctx.getType(name);
                    return type != null && type.isAnnotationPresent(ConfigurationPropertiesBinding.class);
                })
                .sorted()
                .collect(Collectors.joining(", "));
        System.out.println("[BeanInspector] @ConfigurationPropertiesBinding beans: " + cpbBeans);

        // check specific bean names that might be your converters
        Arrays.stream(new String[]{"rsaKeyConverter", "rsaPublicKeyConverter", "rsaPrivateKeyConverter"})
                .forEach(n -> {
                    boolean exists = ctx.containsBean(n);
                    System.out.println("[BeanInspector] containsBean('" + n + "') = " + exists);
                });

        // inspect the configured property value (safe snippet)
        String raw = env.getProperty("security.rsa.public-key");
        if (raw == null) {
            System.out.println("[BeanInspector] security.rsa.public-key = <null>");
        } else {
            String clean = raw.replace("\n", "\\n");
            int len = clean.length();
            String head = clean.substring(0, Math.min(40, len));
            String tail = len > 40 ? clean.substring(Math.max(0, len - 40)) : "";
            System.out.println("[BeanInspector] security.rsa.public-key length=" + len + " head=\"" + head + "\" tail=\"" + tail + "\"");

            // quick base64 sanity check (strip PEM markers if present)
            String maybeBase64 = clean;
            if (maybeBase64.contains("-----BEGIN")) {
                maybeBase64 = maybeBase64.replaceAll("(?s)-----BEGIN[^-]+-----", "")
                        .replaceAll("(?s)-----END[^-]+-----", "")
                        .replaceAll("\\\\n", "")
                        .replaceAll("\\s", "");
            }
            try {
                Base64.getDecoder().decode(maybeBase64);
                System.out.println("[BeanInspector] base64 decode: OK");
            } catch (IllegalArgumentException ex) {
                System.out.println("[BeanInspector] base64 decode: FAILED -> " + ex.getMessage());
            }
        }
    }
}