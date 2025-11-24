package com.quetoquenana.userservice.integration;

import com.quetoquenana.userservice.dto.AddressCreateRequest;
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
class AddressControllerIT {

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

        // Ensure RSA keys and issuer are available for security beans
        registry.add("security.rsa.public-key", () -> "classpath:keys/user_service_public_key.pem");
        registry.add("security.rsa.private-key", () -> "classpath:keys/user_service_private_key.pem");
        registry.add("security.jwt.issuer", () -> "user-service");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private AddressRepository addressRepository;

    // Security-related repositories for seeding
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
    private String payload;

    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        // Clean relevant tables to keep tests idempotent
        appRoleUserRepository.deleteAll();
        appRoleRepository.deleteAll();
        applicationRepository.deleteAll();
        userRepository.deleteAll();
        addressRepository.deleteAll();
        personRepository.deleteAll();

        // Create a Person used by the address tests
        person = TestEntityFactory.createPerson();
        person = personRepository.save(person);

        // Load JSON payload for address create/update
        JsonPayloadToObjectBuilder<AddressCreateRequest> mapper = new JsonPayloadToObjectBuilder<>(AddressCreateRequest.class);
        payload = mapper.loadJsonData("payloads/address-create-request.json");

        // Seed security data (application, role, user mapping). Use username 'user' which is default for @WithMockUser
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
    void addAddressToPerson_andAssertPresent() throws Exception {
        mockMvc.perform(post("/api/persons/" + person.getId() + "/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        List<Address> addresses = addressRepository.findByPersonId(person.getId());
        assertThat(addresses).isNotEmpty();
        Address addedAddress = addresses.getFirst();
        assertThat(addedAddress.getAddress()).isEqualTo("randomStreetAddress");
        assertThat(addedAddress.getCountry()).isEqualTo("randomCountry");
        assertThat(addedAddress.getCity()).isEqualTo("randomCity");
        assertThat(addedAddress.getZipCode()).isEqualTo("28001");
        assertThat(addedAddress.getAddressType()).isEqualTo(AddressType.HOME);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateAddress_andAssertUpdated() throws Exception {
        // Add address first
        Address address = TestEntityFactory.createAddress(person);
        person.addAddress(address);
        person = personRepository.save(person);
        Address savedAddress = addressRepository.findByPersonId(person.getId()).getFirst();

        // Update address
        mockMvc.perform(put("/api/persons/address/" + savedAddress.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        Address updatedAddress = addressRepository.findByPersonId(person.getId()).getFirst();
        assertThat(updatedAddress.getAddress()).isEqualTo("randomStreetAddress");
        assertThat(updatedAddress.getCountry()).isEqualTo("randomCountry");
        assertThat(updatedAddress.getCity()).isEqualTo("randomCity");
        assertThat(updatedAddress.getZipCode()).isEqualTo("28001");
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteAddress_andAssertRemoved() throws Exception {
        // Add address
        Address address = TestEntityFactory.createAddress(person);
        person.addAddress(address);
        person = personRepository.save(person);
        Address savedAddress = addressRepository.findByPersonId(person.getId()).getFirst();

        // Delete address
        mockMvc.perform(delete("/api/persons/address/" + savedAddress.getId()))
                .andExpect(status().isNoContent());

        List<Address> addresses = addressRepository.findByPersonId(person.getId());
        assertThat(addresses).isEmpty();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteNonExistentAddress_andAssertError() throws Exception {
        // Try to delete non-existent address
        mockMvc.perform(delete("/api/persons/address/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createAddress_personInactive_returnsBadRequest() throws Exception {
        // mark person inactive
        person.setIsActive(false);
        personRepository.save(person);

        mockMvc.perform(post("/api/persons/" + person.getId() + "/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateAddress_personInactive_returnsBadRequest() throws Exception {
        // Add address first
        Address address = TestEntityFactory.createAddress(person);
        person.addAddress(address);
        person = personRepository.save(person);
        Address savedAddress = addressRepository.findByPersonId(person.getId()).getFirst();

        // mark person inactive
        person.setIsActive(false);
        personRepository.save(person);

        // attempt update
        mockMvc.perform(put("/api/persons/address/" + savedAddress.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteAddress_personInactive_returnsBadRequest() throws Exception {
        // Add address first
        Address address = TestEntityFactory.createAddress(person);
        person.addAddress(address);
        person = personRepository.save(person);
        Address savedAddress = addressRepository.findByPersonId(person.getId()).getFirst();

        // mark person inactive
        person.setIsActive(false);
        personRepository.save(person);

        // attempt delete
        mockMvc.perform(delete("/api/persons/address/" + savedAddress.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateAddress_notFound_returnsForbidden() throws Exception {
        // random UUID not present
        UUID notFound = UUID.fromString("00000000-0000-0000-0000-000000000001");

        mockMvc.perform(put("/api/persons/address/" + notFound)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createAddress_personNotFound_returnsForbidden() throws Exception {
        // random UUID not present
        UUID notFoundPerson = UUID.fromString("00000000-0000-0000-0000-000000000099");

        mockMvc.perform(post("/api/persons/" + notFoundPerson + "/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }
}
