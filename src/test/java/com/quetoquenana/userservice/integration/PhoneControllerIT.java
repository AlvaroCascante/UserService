package com.quetoquenana.userservice.integration;

import com.quetoquenana.userservice.dto.PhoneCreateRequest;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
class PhoneControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private PhoneRepository phoneRepository;

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
        appRoleUserRepository.deleteAll();
        appRoleRepository.deleteAll();
        applicationRepository.deleteAll();
        userRepository.deleteAll();
        phoneRepository.deleteAll();
        personRepository.deleteAll();

        person = TestEntityFactory.createPerson();
        person = personRepository.save(person);

        JsonPayloadToObjectBuilder<PhoneCreateRequest> mapper = new JsonPayloadToObjectBuilder<>(PhoneCreateRequest.class);
        payload = mapper.loadJsonData("payloads/phone-create-request.json");

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
    void addPhoneToPerson_andAssertPresent() throws Exception {
        mockMvc.perform(post("/api/persons/" + person.getId() + "/phone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        List<Phone> phones = phoneRepository.findByPersonId(person.getId());
        assertThat(phones).isNotEmpty();
        Phone addedPhone = phones.getFirst();
        assertThat(addedPhone.getPhoneNumber()).isEqualTo("1234567890");
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updatePhone_andAssertUpdated() throws Exception {
        Phone phone = TestEntityFactory.createPhone(person, "123456789");
        person.addPhone(phone);
        person = personRepository.save(person);
        Phone savedPhone = phoneRepository.findByPersonId(person.getId()).getFirst();

        // update payload - reuse same payload file that should set phoneNumber to 987654321
        mockMvc.perform(put("/api/persons/phone/" + savedPhone.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        Phone updated = phoneRepository.findByPersonId(person.getId()).getFirst();
        assertThat(updated.getPhoneNumber()).isEqualTo("1234567890");
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteMainPhone_andAssertNotRemoved() throws Exception {
        Phone mainPhone = TestEntityFactory.createPhone(person, "123456789");
        mainPhone.setIsMain(true);
        person.addPhone(mainPhone);
        person = personRepository.save(person);
        Phone savedMain = phoneRepository.findByPersonId(person.getId()).getFirst();

        mockMvc.perform(delete("/api/persons/phone/" + savedMain.getId()))
                .andExpect(status().isBadRequest());

        List<Phone> phones = phoneRepository.findByPersonId(person.getId());
        assertThat(phones).isNotEmpty();
        assertThat(phones.getFirst().getIsMain()).isTrue();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteNonMainPhone_andAssertRemoved() throws Exception {
        Phone mainPhone = TestEntityFactory.createPhone(person, "123456789");
        mainPhone.setIsMain(true);
        person.addPhone(mainPhone);

        Phone secondaryPhone = TestEntityFactory.createPhone(person, "987654321");
        secondaryPhone.setIsMain(false);
        person.addPhone(secondaryPhone);

        person = personRepository.save(person);

        Phone savedSecondary = phoneRepository.findByPersonId(person.getId()).stream().filter(p -> !p.getIsMain()).findFirst().orElseThrow();

        mockMvc.perform(delete("/api/persons/phone/" + savedSecondary.getId()))
                .andExpect(status().isNoContent());

        List<Phone> phones = phoneRepository.findByPersonId(person.getId());
        assertThat(phones).hasSize(1);
        assertThat(phones.getFirst().getIsMain()).isTrue();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createPhone_personInactive_returnsBadRequest() throws Exception {
        person.setIsActive(false);
        personRepository.save(person);

        mockMvc.perform(post("/api/persons/" + person.getId() + "/phone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updatePhone_personInactive_returnsBadRequest() throws Exception {
        Phone phone = TestEntityFactory.createPhone(person, "123456789");
        person.addPhone(phone);
        person = personRepository.save(person);
        Phone savedPhone = phoneRepository.findByPersonId(person.getId()).getFirst();

        person.setIsActive(false);
        personRepository.save(person);

        mockMvc.perform(put("/api/persons/phone/" + savedPhone.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deletePhone_personInactive_returnsBadRequest() throws Exception {
        Phone phone = TestEntityFactory.createPhone(person, "123456789");
        person.addPhone(phone);
        person = personRepository.save(person);
        Phone savedPhone = phoneRepository.findByPersonId(person.getId()).getFirst();

        person.setIsActive(false);
        personRepository.save(person);

        mockMvc.perform(delete("/api/persons/phone/" + savedPhone.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updatePhone_notFound_returnsForbidden() throws Exception {
        UUID notFound = UUID.fromString("00000000-0000-0000-0000-000000000002");

        mockMvc.perform(put("/api/persons/phone/" + notFound)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createPhone_personNotFound_returnsForbidden() throws Exception {
        UUID notFoundPerson = UUID.fromString("00000000-0000-0000-0000-000000000099");

        mockMvc.perform(post("/api/persons/" + notFoundPerson + "/phone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }
}
