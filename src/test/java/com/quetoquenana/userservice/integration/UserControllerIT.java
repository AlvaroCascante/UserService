package com.quetoquenana.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quetoquenana.userservice.dto.UserCreateRequest;
import com.quetoquenana.userservice.dto.UserUpdateRequest;
import com.quetoquenana.userservice.model.Person;
import com.quetoquenana.userservice.model.User;
import com.quetoquenana.userservice.model.UserStatus;
import com.quetoquenana.userservice.repository.*;
import com.quetoquenana.userservice.util.TestDataSeeder;
import com.quetoquenana.userservice.util.TestEntityFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

        // RSA keys + issuer for security beans
        registry.add("security.rsa.public-key", () -> "classpath:keys/user_service_public_key.pem");
        registry.add("security.rsa.private-key", () -> "classpath:keys/user_service_private_key.pem");
        registry.add("security.jwt.issuer", () -> "user-service");
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private AppRoleRepository appRoleRepository;
    @Autowired
    private AppRoleUserRepository appRoleUserRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // keep DB clean and idempotent
        appRoleUserRepository.deleteAll();
        appRoleRepository.deleteAll();
        applicationRepository.deleteAll();
        userRepository.deleteAll();
        personRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = {"SYSTEM"})
    void createUser_asSystem_returnsCreated() throws Exception {
        UserCreateRequest req = TestEntityFactory.getUserCreateRequest();
        String json = objectMapper.writeValueAsString(req);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username").value(req.getUsername()));

        assertThat(userRepository.findByUsernameIgnoreCase(req.getUsername())).isPresent();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getUserById_canAccess() throws Exception {
        // create person and seed user mapping
        Person person = TestEntityFactory.createPerson();
        person = personRepository.save(person);

        User seeded = TestDataSeeder.seedUserWithRole(
                applicationRepository,
                appRoleRepository,
                userRepository,
                appRoleUserRepository,
                passwordEncoder,
                person,
                "user-service",
                "ADMIN",
                "user",
                "password"
        );

        mockMvc.perform(get("/api/users/" + seeded.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(seeded.getId().toString()));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateUser_asAdmin_updatesNickname() throws Exception {
        Person person = TestEntityFactory.createPerson();
        person = personRepository.save(person);

        User seeded = TestDataSeeder.seedUserWithRole(
                applicationRepository,
                appRoleRepository,
                userRepository,
                appRoleUserRepository,
                passwordEncoder,
                person,
                "user-service",
                "ADMIN",
                "user",
                "password"
        );

        UserUpdateRequest update = TestEntityFactory.getUserUpdateRequest();
        update.setNickname("new-nick");
        update.setUserStatus(UserStatus.ACTIVE.name());
        String json = objectMapper.writeValueAsString(update);

        mockMvc.perform(put("/api/users/" + seeded.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("new-nick"));

        User updated = userRepository.findById(seeded.getId()).orElseThrow();
        assertThat(updated.getNickname()).isEqualTo("new-nick");
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteUser_asAdmin_removesUser() throws Exception {
        Person person = TestEntityFactory.createPerson();
        person = personRepository.save(person);

        User seeded = TestDataSeeder.seedUserWithRole(
                applicationRepository,
                appRoleRepository,
                userRepository,
                appRoleUserRepository,
                passwordEncoder,
                person,
                "user-service",
                "ADMIN",
                "user",
                "password"
        );

        mockMvc.perform(delete("/api/users/" + seeded.getId()))
                .andExpect(status().isNoContent());

        User deleted = userRepository.findById(seeded.getId()).orElseThrow();
        assertThat(deleted.getUserStatus()).isEqualTo(UserStatus.INACTIVE);
    }
}
