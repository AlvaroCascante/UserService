package com.quetoquenana.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quetoquenana.userservice.dto.ResetPasswordRequest;
import com.quetoquenana.userservice.dto.UserUpdateRequest;
import com.quetoquenana.userservice.model.Application;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
class UserControllerIT extends AbstractIntegrationTest {

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


    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getUserApplicationDetails_asAdmin_returnsAppRoles() throws Exception {
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

        // retrieve the created application id
        Application app = applicationRepository.findAll().getFirst();

        mockMvc.perform(get("/api/users/" + seeded.getId() + "/" + app.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getUsersPage_asAdmin_returnsPagedUsers() throws Exception {
        // seed two users so page has content
        Person p1 = personRepository.save(TestEntityFactory.createPerson());

        TestDataSeeder.seedUserWithRole(
                applicationRepository,
                appRoleRepository,
                userRepository,
                appRoleUserRepository,
                passwordEncoder,
                p1,
                "user-service",
                "ADMIN",
                "user1",
                "password1"
        );

        mockMvc.perform(get("/api/users/page")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").exists());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getByUsername_asAdmin_returnsUser() throws Exception {
        Person person = personRepository.save(TestEntityFactory.createPerson());

        String username = "unique-username";
        User seeded = TestDataSeeder.seedUserWithRole(
                applicationRepository,
                appRoleRepository,
                userRepository,
                appRoleUserRepository,
                passwordEncoder,
                person,
                "user-service",
                "ADMIN",
                username,
                "password"
        );

        mockMvc.perform(get("/api/users/username/" + username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value(username))
                .andExpect(jsonPath("$.data.id").value(seeded.getId().toString()));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void resetPassword_asAdmin_changesPassword() throws Exception {
        Person person = personRepository.save(TestEntityFactory.createPerson());

        User seeded = TestDataSeeder.seedUserWithRole(
                applicationRepository,
                appRoleRepository,
                userRepository,
                appRoleUserRepository,
                passwordEncoder,
                person,
                "user-service",
                "ADMIN",
                "reset-user",
                "old-password"
        );

        String newPassword = "new-secret-password";
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setNewPassword(newPassword);

        mockMvc.perform(post("/api/users/" + seeded.getId() + "/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());

        User updated = userRepository.findById(seeded.getId()).orElseThrow();
        assertThat(passwordEncoder.matches(newPassword, updated.getPasswordHash())).isTrue();
    }
}
