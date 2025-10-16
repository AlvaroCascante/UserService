package com.quetoquenana.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quetoquenana.userservice.dto.UserCreateRequest;
import com.quetoquenana.userservice.dto.UserUpdateRequest;
import com.quetoquenana.userservice.model.Person;
import com.quetoquenana.userservice.model.User;
import com.quetoquenana.userservice.model.UserStatus;
import com.quetoquenana.userservice.repository.PersonRepository;
import com.quetoquenana.userservice.repository.UserRepository;
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
import java.util.Optional;
import java.util.UUID;

import static com.quetoquenana.userservice.util.TestEntityFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ExtendWith(SpringExtension.class)
class UserControllerIT {
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
    private UserRepository userRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/users";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        personRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = DEFAULT_USER, roles = {ROLE_ADMIN})
    void testCreateUser_SetsAuditableFields() throws Exception {
        String json = TestEntityFactory.createUserPayload(objectMapper);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());

        Optional<User> saved = userRepository.findByUsername("username");
        assertThat(saved).isPresent();
        assertThat(saved.get().getCreatedBy()).isEqualTo(DEFAULT_USER);
        assertThat(saved.get().getCreatedAt()).isNotNull();
    }

    @Test
    @WithMockUser(username = DEFAULT_USER, roles = {ROLE_ADMIN})
    void testUpdateUser_SetsAuditableFields() throws Exception {
        User user = TestEntityFactory.createUser(LocalDateTime.now().minusDays(1), "creator");
        userRepository.save(user);
        userRepository.flush();

        UserUpdateRequest updateReq = new UserUpdateRequest();
        updateReq.setNickname("NewNick");
        updateReq.setUserStatus(UserStatus.INACTIVE.name());

        String json = objectMapper.writeValueAsString(updateReq);

        mockMvc.perform(put(BASE_URL + "/" + user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getUpdatedBy()).isEqualTo(DEFAULT_USER);
        assertThat(updated.getUpdatedAt()).isNotNull();
        assertThat(updated.getNickname()).isEqualTo("NewNick");
        assertThat(updated.getUserStatus()).isEqualTo(UserStatus.INACTIVE);
    }

    @Test
    @WithMockUser(username = DEFAULT_USER, roles = {ROLE_ADMIN})
    void testDeleteUser_RemovesRecord() throws Exception {
        User user = TestEntityFactory.createUser(LocalDateTime.now().minusDays(1), "creator");
        userRepository.save(user);
        userRepository.flush();
        user = userRepository.findByUsername("username").orElseThrow();

        mockMvc.perform(delete(BASE_URL + "/" + user.getId()))
                .andExpect(status().isNoContent());

        User deletedUser = userRepository.findById(user.getId()).orElse(null);
        assertThat(deletedUser).isNotNull();
        assertThat(deletedUser.getUserStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(deletedUser.getUpdatedBy()).isEqualTo(DEFAULT_USER);
        assertThat(deletedUser.getUpdatedAt()).isNotNull();
        assertThat(deletedUser.getCreatedBy()).isEqualTo("creator");
    }

    @Test
    @WithMockUser(username = DEFAULT_USER, roles = {ROLE_ADMIN})
    void testResetPassword_SetsAuditableFields() throws Exception {
        User user = TestEntityFactory.createUser(LocalDateTime.now().minusDays(1), "creator");
        userRepository.save(user);
        userRepository.flush();
        user = userRepository.findByUsername("username").orElseThrow();

        String json = "{\"newPassword\":\"newpass\"}";

        mockMvc.perform(post(BASE_URL + "/" + user.getId() + "/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNoContent());

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getUpdatedBy()).isEqualTo(DEFAULT_USER);
        assertThat(updated.getUpdatedAt()).isNotNull();
        assertThat(updated.getPasswordHash()).isNotEqualTo("oldhash");
    }

    @Test
    @WithMockUser(username = DEFAULT_USER, roles = {ROLE_ADMIN})
    void updateNonExistentUser_shouldReturnNotFound() throws Exception {
        UserUpdateRequest updateReq = new UserUpdateRequest();
        updateReq.setNickname("x");
        String json = objectMapper.writeValueAsString(updateReq);
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(put(BASE_URL + "/" + nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = DEFAULT_USER, roles = {ROLE_ADMIN})
    void testCreateUser_WithExistingPerson_linksToPerson() throws Exception {
        // Arrange: create and persist a Person first
        Person person = TestEntityFactory.createPerson(DEFAULT_ID_NUMBER, false);
        personRepository.save(person);
        personRepository.flush();

        String json = TestEntityFactory.createUserPayload(objectMapper);

        // Act: create the user
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());

        // Assert: user was created and linked to the existing person
        Optional<User> saved = userRepository.findByUsername("username");
        assertThat(saved).isPresent();
        User created = saved.get();
        assertThat(created.getPerson()).isNotNull();
        assertThat(created.getPerson().getId()).isEqualTo(person.getId());
        assertThat(created.getCreatedBy()).isEqualTo(DEFAULT_USER);
        assertThat(created.getCreatedAt()).isNotNull();
    }

    @Test
    @WithMockUser(username = DEFAULT_USER, roles = {ROLE_ADMIN})
    void testCreateUser_DuplicateUsername_returnsConflict() throws Exception {
        // First create should succeed
        String json = TestEntityFactory.createUserPayload(objectMapper);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());

        // Second create with same payload (same username) should fail with 409 Conflict
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict());
    }
}
