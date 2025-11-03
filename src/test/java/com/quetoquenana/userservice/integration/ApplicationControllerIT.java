package com.quetoquenana.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quetoquenana.userservice.model.Application;
import com.quetoquenana.userservice.repository.ApplicationRepository;
import com.quetoquenana.userservice.util.TestEntityFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.quetoquenana.userservice.util.TestEntityFactory.DEFAULT_USER;
import static com.quetoquenana.userservice.util.TestEntityFactory.ROLE_ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ExtendWith(SpringExtension.class)
class ApplicationControllerIT {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/applications";

    @BeforeEach
    void setUp() {
        applicationRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = DEFAULT_USER, roles = {ROLE_ADMIN})
    void testCreateApplication_SetsAuditableFields() throws Exception {
        // build payload
        var payload = new java.util.HashMap<String, Object>();
        payload.put("name", "it-app");
        payload.put("description", "Integration test app");
        payload.put("isActive", true);

        String json = objectMapper.writeValueAsString(payload);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());

        Application saved = applicationRepository.findByName("it-app").orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getCreatedBy()).isEqualTo(DEFAULT_USER);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    @WithMockUser(username = DEFAULT_USER, roles = {ROLE_ADMIN})
    void createApplication_withDuplicateName_shouldReturnConflict() throws Exception {
        // arrange: existing application
        Application app = Application.builder()
                .name("dup-app")
                .description("dup")
                .isActive(true)
                .build();
        app.setCreatedAt(LocalDateTime.now());
        app.setCreatedBy("creator");
        applicationRepository.save(app);
        applicationRepository.flush();

        var payload = new java.util.HashMap<String, Object>();
        payload.put("name", "dup-app");
        payload.put("description", "dup");
        payload.put("isActive", true);

        String json = objectMapper.writeValueAsString(payload);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = DEFAULT_USER, roles = {ROLE_ADMIN})
    void updateNonExistentApplication_shouldReturnNotFound() throws Exception {
        var payload = new java.util.HashMap<String, Object>();
        payload.put("name", "nonexistent");

        String json = objectMapper.writeValueAsString(payload);
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(put(BASE_URL + "/" + nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = DEFAULT_USER, roles = {ROLE_ADMIN})
    void deleteNonExistentApplication_shouldReturnNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(delete(BASE_URL + "/" + nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = DEFAULT_USER, roles = {ROLE_ADMIN})
    void getApplicationByName_returnsApplication() throws Exception {
        Application app = Application.builder()
                .name("find-me")
                .description("find")
                .isActive(true)
                .build();
        app.setCreatedAt(LocalDateTime.now());
        app.setCreatedBy("creator");
        applicationRepository.save(app);
        applicationRepository.flush();

        mockMvc.perform(get(BASE_URL + "/name/{name}", "find-me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("find-me")));
    }

    @Test
    @WithMockUser(username = DEFAULT_USER, roles = {ROLE_ADMIN})
    void getAllApplications_returnsList() throws Exception {
        Application app = Application.builder()
                .name("list-app")
                .description("list")
                .isActive(true)
                .build();
        app.setCreatedAt(LocalDateTime.now());
        app.setCreatedBy("creator");
        applicationRepository.save(app);
        applicationRepository.flush();

        mockMvc.perform(get(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("list-app")));
    }
}

