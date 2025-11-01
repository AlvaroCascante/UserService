package com.quetoquenana.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quetoquenana.userservice.model.Person;
import com.quetoquenana.userservice.model.Phone;
import com.quetoquenana.userservice.repository.PersonRepository;
import com.quetoquenana.userservice.repository.PhoneRepository;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ExtendWith(SpringExtension.class)
class PhoneControllerIT {
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
    private PhoneRepository phoneRepository;

    private Person person;

    @BeforeEach
    void setUp() {
        personRepository.deleteAll();
        person = TestEntityFactory.createPerson();
        person = personRepository.save(person);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void addPhoneToPerson_andAssertPresent() throws Exception {
        String payload = TestEntityFactory.createPhonePayload(objectMapper, "123456789");

        mockMvc.perform(post("/api/persons/" + person.getId() + "/phone")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());

        List<Phone> phones = phoneRepository.findByPersonId(person.getId());
        assertThat(phones).isNotEmpty();
        Phone addedPhone = phones.getFirst();
        assertThat(addedPhone.getPhoneNumber()).isEqualTo("123456789");
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updatePhone_andAssertUpdated() throws Exception {
        // Add phone first
        Phone phone = TestEntityFactory.createPhone(person, "123456789");
        person.addPhone(phone);
        person = personRepository.save(person);
        Phone savedPhone = phoneRepository.findByPersonId(person.getId()).getFirst();

        // Update phone
        savedPhone.setPhoneNumber("987654321");
        String payload = TestEntityFactory.createPhonePayload(objectMapper, "987654321");
        mockMvc.perform(put("/api/persons/phone/" + savedPhone.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());

        Phone updatedPhone = phoneRepository.findByPersonId(person.getId()).getFirst();
        assertThat(updatedPhone.getPhoneNumber()).isEqualTo("987654321");
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteMainPhone_andAssertNotRemoved() throws Exception {
        // Add main phone
        Phone mainPhone = TestEntityFactory.createPhone(person, "123456789");
        mainPhone.setMain(true);
        person.addPhone(mainPhone);
        person = personRepository.save(person);
        Phone savedMainPhone = phoneRepository.findByPersonId(person.getId()).getFirst();

        // Try to delete main phone and assert error response
        mockMvc.perform(delete("/api/persons/phone/" + savedMainPhone.getId()))
                .andExpect(status().isBadRequest());

        List<Phone> phones = phoneRepository.findByPersonId(person.getId());
        assertThat(phones).isNotEmpty();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteNonMainPhone_andAssertRemoved() throws Exception {
        // Add main phone
        Phone mainPhone = TestEntityFactory.createPhone(person, "123456789");
        mainPhone.setMain(true);
        person.addPhone(mainPhone);

        // Add secondary phone
        Phone secondaryPhone = TestEntityFactory.createPhone(person, "987654321");
        secondaryPhone.setMain(false);
        person.addPhone(secondaryPhone);

        person = personRepository.save(person);

        // Find secondary phone
        Phone savedSecondaryPhone = phoneRepository.findByPersonId(person.getId())
                .stream().filter(p -> !p.isMain()).findFirst().orElseThrow();

        // Delete secondary phone
        mockMvc.perform(delete("/api/persons/phone/" + savedSecondaryPhone.getId()))
                .andExpect(status().isNoContent());

        List<Phone> phones = phoneRepository.findByPersonId(person.getId());
        assertThat(phones).hasSize(1);
        assertThat(phones.getFirst().isMain()).isTrue();
    }
}
