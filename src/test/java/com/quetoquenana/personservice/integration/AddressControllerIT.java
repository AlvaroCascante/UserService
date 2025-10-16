package com.quetoquenana.personservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quetoquenana.personservice.model.Person;
import com.quetoquenana.personservice.model.Address;
import com.quetoquenana.personservice.model.AddressType;
import com.quetoquenana.personservice.repository.PersonRepository;
import com.quetoquenana.personservice.repository.AddressRepository;
import com.quetoquenana.personservice.util.TestEntityFactory;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private AddressRepository addressRepository;

    private Person person;

    @BeforeEach
    void setUp() {
        addressRepository.deleteAll();
        personRepository.deleteAll();
        person = TestEntityFactory.createPerson();
        person = personRepository.save(person);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void addAddressToPerson_andAssertPresent() throws Exception {
        String payload = TestEntityFactory.createAddressPayload(objectMapper);

        mockMvc.perform(post("/api/persons/" + person.getId() + "/address")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());

        List<Address> addresses = addressRepository.findByPersonId(person.getId());
        assertThat(addresses).isNotEmpty();
        Address addedAddress = addresses.getFirst();
        assertThat(addedAddress.getCountry()).isEqualTo("Country");
        assertThat(addedAddress.getCity()).isEqualTo("City");
        assertThat(addedAddress.getZipCode()).isEqualTo("12345");
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
        savedAddress.setCity("Barcelona");
        String payload = TestEntityFactory.createAddressPayload(objectMapper, "Spain");
        mockMvc.perform(put("/api/persons/address/" + savedAddress.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());

        Address updatedAddress = addressRepository.findByPersonId(person.getId()).getFirst();
        assertThat(updatedAddress.getCountry()).isEqualTo("Spain");
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
        mockMvc.perform(delete("/api/persons/address/999999"))
                .andExpect(status().isBadRequest());
    }
}

