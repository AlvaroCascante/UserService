package com.quetoquenana.personservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quetoquenana.personservice.model.Person;
import com.quetoquenana.personservice.model.Profile;
import com.quetoquenana.personservice.repository.ProfileRepository;
import com.quetoquenana.personservice.repository.PersonRepository;
import com.quetoquenana.personservice.util.TestEntityFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import static com.quetoquenana.personservice.util.TestEntityFactory.DEFAULT_USER;
import static com.quetoquenana.personservice.util.TestEntityFactory.ROLE_ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ExtendWith(SpringExtension.class)
class ProfileControllerIT {
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
    private PersonRepository personRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private Person person;

    private static final Logger log = LoggerFactory.getLogger(ProfileControllerIT.class);

    @BeforeEach
    void setUp() {
        profileRepository.deleteAll();
        personRepository.deleteAll();
        person = TestEntityFactory.createPerson();
        personRepository.save(person);
        personRepository.flush(); // Ensure Hibernate flushes the insert
        person = personRepository.findById(person.getId()).orElseThrow();

        assertThat(person.getVersion()).isNotNull();
        assertThat(person.getVersion()).isEqualTo(0L);
    }

    @Test
    @WithMockUser(username = DEFAULT_USER, roles = {ROLE_ADMIN})
    void testCreateUpdateProfile_SavesCreatedByAndCreatedAt() throws Exception {
        // Creation
        String createJson = TestEntityFactory.createProfilePayload(objectMapper);

        LocalDateTime before = LocalDateTime.now();
        mockMvc.perform(post("/api/persons/" + person.getId() + "/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createJson))
                .andExpect(status().isOk());
        LocalDateTime afterCreate = LocalDateTime.now();

        Profile savedProfile = profileRepository.findAll().stream().findFirst().orElse(null);
        assertThat(savedProfile).isNotNull();
        assertThat(savedProfile.getCreatedBy()).isEqualTo(DEFAULT_USER);
        assertThat(savedProfile.getCreatedAt()).isNotNull();
        assertThat(!savedProfile.getCreatedAt().isBefore(before) && !savedProfile.getCreatedAt().isAfter(afterCreate)).isTrue();

        // Update
        String updateJson = TestEntityFactory.createProfilePayload(objectMapper);
        mockMvc.perform(put("/api/persons/profile/" + savedProfile.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());
        LocalDateTime afterUpdate = LocalDateTime.now();

        Profile updatedProfile = profileRepository.findAll().stream().findFirst().orElse(null);

        assertThat(updatedProfile).isNotNull();
        assertThat(updatedProfile.getUpdatedBy()).isEqualTo(DEFAULT_USER);
        assertThat(updatedProfile.getUpdatedAt()).isNotNull();
        assertThat(!updatedProfile.getUpdatedAt().isBefore(before) && !updatedProfile.getUpdatedAt().isAfter(afterUpdate)).isTrue();
    }

    @Test
    @WithMockUser(username = DEFAULT_USER, roles = {ROLE_ADMIN})
    void testAddProfileToNonExistentPerson_ShouldReturnNotFound() throws Exception {
        String createJson = TestEntityFactory.createProfilePayload(objectMapper);
        // Use a random UUID that does not exist
        String nonExistentId = java.util.UUID.randomUUID().toString();

        mockMvc.perform(post("/api/persons/" + nonExistentId + "/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createJson))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = DEFAULT_USER, roles = {ROLE_ADMIN})
    void testAddDuplicateProfile_ShouldReturnConflict() throws Exception {
        String createJson = TestEntityFactory.createProfilePayload(objectMapper);

        // First creation should succeed
        mockMvc.perform(post("/api/persons/" + person.getId() + "/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createJson))
                .andExpect(status().isOk());

        // Second creation should fail (duplicate)
        mockMvc.perform(post("/api/persons/" + person.getId() + "/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createJson))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = DEFAULT_USER, roles = {ROLE_ADMIN})
    void testUpdateNonExistentProfile_ShouldReturnNotFound() throws Exception {
        String updateJson = TestEntityFactory.createProfilePayload(objectMapper);
        String nonExistentProfileId = java.util.UUID.randomUUID().toString();

        mockMvc.perform(put("/api/persons/profile/" + nonExistentProfileId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isNotFound());
    }
}
