package com.quetoquenana.userservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class StartupLogger implements ApplicationRunner {

    private final Environment env;
    private final Logger log = LoggerFactory.getLogger(StartupLogger.class);

    public StartupLogger(Environment env) {
        this.env = env;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("--- Application properties (selected) ---");

        // configurable comma-separated list of property keys to log; sensible defaults below
        String keysProp = env.getProperty("startup.log.keys");
        String[] keys = getKeys(keysProp);

        for (String key : keys) {
            if (key == null || key.isBlank()) continue;
            String value = env.getProperty(key);
            if (value == null) {
                log.info("{} = <not set>", key);
            } else if (looksLikeSecretKey(key) || key.toLowerCase().contains("password") || key.toLowerCase().contains("secret")) {
                log.info("{} = {}", key, mask(value));
            } else {
                log.info("{} = {}", key, value);
            }
        }

        log.info("--- End of properties ---");
    }

    private static String[] getKeys(String keysProp) {
        String[] keys;
        if (keysProp != null && !keysProp.isBlank()) {
            keys = keysProp.split("\\s*,\\s*");
        } else {
            keys = new String[]{
                    "spring.datasource.url",
                    "spring.datasource.username",
                    "app.support.email",
                    "spring.mail.host",
                    "spring.mail.port",
                    "spring.mail.username"
            };
        }
        return keys;
    }

    private boolean looksLikeSecretKey(String key) {
        if (key == null) return false;
        String k = key.toLowerCase();
        return k.contains("password") || k.contains("secret") || k.contains("key") || k.contains("token");
    }

    private String mask(String s) {
        if (s == null) return null;
        // First try to mask common query param password=... patterns
        try {
            String masked = s.replaceAll("(?i)(password=)[^&]*", "$1****");

            // If URL-style user:pass@host, mask the credentials portion.
            int idx = masked.indexOf("://");
            if (idx >= 0) {
                int at = masked.indexOf('@', idx + 3);
                if (at > 0) {
                    // keep prefix up to scheme:// then hide credentials
                    String prefix = masked.substring(0, idx + 3);
                    String suffix = masked.substring(at + 1);
                    return prefix + "****@" + suffix;
                }
            }

            if (!masked.equals(s)) return masked;

            // Fallback: show first 4 and last 4 characters
            if (s.length() <= 8) return "****";
            return s.substring(0, 4) + "****" + s.substring(s.length() - 4);
        } catch (Exception e) {
            return "****";
        }
    }
}

