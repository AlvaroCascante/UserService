package com.quetoquenana.userservice.integration;

import com.quetoquenana.userservice.dto.ProfileCreateRequest;
import com.quetoquenana.userservice.dto.ProfileUpdateRequest;
import com.quetoquenana.userservice.model.*;
import com.quetoquenana.userservice.repository.*;
import com.quetoquenana.userservice.util.JsonPayloadToObjectBuilder;
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
class ProfileControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private ProfileRepository profileRepository;

    // security seeding
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private AppRoleRepository appRoleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AppRoleUserRepository appRoleUserRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Person person;
    private String createPayload;

    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        appRoleUserRepository.deleteAll();
        appRoleRepository.deleteAll();
        applicationRepository.deleteAll();
        userRepository.deleteAll();
        profileRepository.deleteAll();
        personRepository.deleteAll();

        person = TestEntityFactory.createPerson();
        person = personRepository.save(person);

        JsonPayloadToObjectBuilder<ProfileCreateRequest> mapper = new JsonPayloadToObjectBuilder<>(ProfileCreateRequest.class);
        createPayload = mapper.loadJsonData("payloads/profile-create-request.json");

        TestDataSeeder.seedUserWithRole(
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
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void addProfile_andAssertPresent() throws Exception {
        mockMvc.perform(post("/api/persons/" + person.getId() + "/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profilePictureUrl").isNotEmpty());

        // verify persisted
        Profile p = profileRepository.findByPersonId(person.getId()).orElseThrow();
        assertThat(p.getNationality()).isEqualTo("Canadian");
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateProfile_andAssertUpdated() throws Exception {
        // create profile first
        Profile profile = TestEntityFactory.createProfile(person);
        person.setProfile(profile);
        person = personRepository.save(person);
        Profile saved = profileRepository.findByPersonId(person.getId()).orElseThrow();

        ProfileUpdateRequest update = new ProfileUpdateRequest();
        update.setOccupation("Architect");
        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(update);

        mockMvc.perform(put("/api/persons/profile/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        Profile updated = profileRepository.findByPersonId(person.getId()).orElseThrow();
        assertThat(updated.getOccupation()).isEqualTo("Architect");
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createProfile_personInactive_returnsBadRequest() throws Exception {
        person.setIsActive(false);
        personRepository.save(person);

        mockMvc.perform(post("/api/persons/" + person.getId() + "/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateProfile_notFound_returnsForbidden() throws Exception {
        UUID notFound = UUID.fromString("00000000-0000-0000-0000-000000000003");
        ProfileUpdateRequest update = new ProfileUpdateRequest();
        update.setOccupation("Architect");
        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(update);

        mockMvc.perform(put("/api/persons/profile/" + notFound)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createProfile_personNotFound_returnsForbidden() throws Exception {
        UUID notFoundPerson = UUID.fromString("00000000-0000-0000-0000-000000000099");

        mockMvc.perform(post("/api/persons/" + notFoundPerson + "/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isForbidden());
    }
}
