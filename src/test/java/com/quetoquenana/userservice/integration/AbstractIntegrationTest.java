package com.quetoquenana.userservice.integration;

import org.testcontainers.containers.PostgreSQLContainer;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Import(TestMailConfig.class)
public abstract class AbstractIntegrationTest {

    // Manage the container lifecycle manually so it remains up across all integration test classes.
    public static final PostgreSQLContainer<?> postgres;

    static {
        postgres = new PostgreSQLContainer<>("postgres:15-alpine")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");
        // start immediately and register a shutdown hook so it isn't stopped between test classes
        postgres.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                postgres.stop();
            } catch (Throwable ignored) {
            }
        }));
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Ensure the Testcontainers Postgres instance is started before we read its JDBC properties.
        if (!postgres.isRunning()) {
            postgres.start();
        }

        // Wait until the database is accepting JDBC connections (prevents Hikari timeouts)
        waitForJdbc(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword(), 60_000);

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // RSA keys + issuer for security beans
        // Prefer environment variables (Railway / CI) carrying single-line base64 values
        // (e.g. SECURITY_RSA_PUBLIC_KEY=base64:..., SECURITY_RSA_PRIVATE_KEY=base64:...)
        // Fallback to classpath resources for local test development.
        registry.add("security.rsa.public-key", () -> {
            String v = System.getenv("SECURITY_RSA_PUBLIC_KEY");
            return (v != null && !v.isBlank()) ? v : "classpath:keys/user_service_public_key.pem";
        });
        registry.add("security.rsa.private-key", () -> {
            String v = System.getenv("SECURITY_RSA_PRIVATE_KEY");
            return (v != null && !v.isBlank()) ? v : "classpath:keys/user_service_private_key.pem";
        });
        registry.add("security.jwt.issuer", () -> {
            String v = System.getenv("SECURITY_JWT_ISSUER");
            return (v != null && !v.isBlank()) ? v : "user-service";
        });
    }

    private static void waitForJdbc(String jdbcUrl, String username, String password, long timeoutMs) {
        long start = System.currentTimeMillis();
        long deadline = start + timeoutMs;
        SQLException last = null;
        while (System.currentTimeMillis() < deadline) {
            try (Connection c = DriverManager.getConnection(jdbcUrl, username, password)) {
                // success
                return;
            } catch (SQLException e) {
                last = e;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while waiting for JDBC to become available", ie);
                }
            }
        }
        throw new IllegalStateException("Timed out waiting for JDBC at " + jdbcUrl, last);
    }
}
