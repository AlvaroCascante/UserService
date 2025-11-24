package com.quetoquenana.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quetoquenana.userservice.dto.PersonCreateRequest;
import com.quetoquenana.userservice.dto.PersonUpdateRequest;
import com.quetoquenana.userservice.model.Person;
import com.quetoquenana.userservice.model.User;
import com.quetoquenana.userservice.model.UserStatus;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ExtendWith(SpringExtension.class)
class PersonControllerIT {

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
    private AddressRepository addressRepository;

    // seeding
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private AppRoleRepository appRoleRepository;
    @Autowired
    private AppRoleUserRepository appRoleUserRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private String createPayload;
    private Person person;

    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        appRoleUserRepository.deleteAll();
        appRoleRepository.deleteAll();
        applicationRepository.deleteAll();
        userRepository.deleteAll();
        addressRepository.deleteAll();
        personRepository.deleteAll();

        JsonPayloadToObjectBuilder<PersonCreateRequest> mapper = new JsonPayloadToObjectBuilder<>(PersonCreateRequest.class);
        createPayload = mapper.loadJsonData("payloads/person-create-request.json");

        // create a base person used for operations that require an existing person
        person = TestEntityFactory.createPerson();
        person = personRepository.save(person);

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
    void createPerson_andAssertPresent() throws Exception {
        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("randomFirstName"));

        List<Person> people = personRepository.findAll();
        assertThat(people).isNotEmpty();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getPersonById_canAccess() throws Exception {
        mockMvc.perform(get("/api/persons/" + person.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(person.getId().toString()));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updatePerson_andAssertUpdated() throws Exception {
        PersonUpdateRequest update = new PersonUpdateRequest();
        update.setName("UpdatedName");
        String json = objectMapper.writeValueAsString(update);

        mockMvc.perform(put("/api/persons/" + person.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("UpdatedName"));

        Person updated = personRepository.findById(person.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("UpdatedName");
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deletePerson_andAssertRemoved() throws Exception {
        mockMvc.perform(delete("/api/persons/" + person.getId()))
                .andExpect(status().isNoContent());

        assertThat(personRepository.findById(person.getId())).isEmpty();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createPerson_personInactive_returnsBadRequest() throws Exception {
        // mark person inactive and try to create address? Instead test updating when inactive
        person.setIsActive(false);
        personRepository.save(person);

        mockMvc.perform(put("/api/persons/" + person.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PersonUpdateRequest())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getPerson_notFound_returnsForbidden() throws Exception {
        UUID notFound = UUID.fromString("00000000-0000-0000-0000-000000000099");
        mockMvc.perform(get("/api/persons/" + notFound))
                .andExpect(status().isForbidden());
    }
}

