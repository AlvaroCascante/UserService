package com.quetoquenana.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quetoquenana.userservice.dto.PersonUpdateRequest;
import com.quetoquenana.userservice.model.Person;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
class PersonControllerIT extends AbstractIntegrationTest {

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

    private Person person;

    @BeforeEach
    void setUp() {
        appRoleUserRepository.deleteAll();
        appRoleRepository.deleteAll();
        applicationRepository.deleteAll();
        userRepository.deleteAll();
        addressRepository.deleteAll();
        personRepository.deleteAll();

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

    @WithMockUser(roles = {"ADMIN"})
    @Test
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
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getPerson_notFound_returnsForbidden() throws Exception {
        UUID notFound = UUID.fromString("00000000-0000-0000-0000-000000000099");
        mockMvc.perform(get("/api/persons/" + notFound))
                .andExpect(status().isForbidden());
    }
}
