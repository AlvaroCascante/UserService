package com.quetoquenana.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quetoquenana.userservice.dto.DefaultDataCreateRequest;
import com.quetoquenana.userservice.dto.DefaultDataUpdateRequest;
import com.quetoquenana.userservice.model.DefaultData;
import com.quetoquenana.userservice.model.DataCategory;
import com.quetoquenana.userservice.model.UserStatus;
import com.quetoquenana.userservice.repository.*;
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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
class DefaultDataControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DefaultDataRepository defaultDataRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        defaultDataRepository.deleteAll();
        applicationRepository.deleteAll();
        userRepository.deleteAll();
        personRepository.deleteAll();

        // seed a system user so currentUserService lookups succeed
        var person = TestEntityFactory.createPerson();
        person = personRepository.save(person);
        var systemUser = com.quetoquenana.userservice.model.User.builder()
                .username("system")
                .passwordHash(passwordEncoder.encode("password"))
                .person(person)
                .nickname("system")
                .userStatus(UserStatus.ACTIVE)
                .build();
        systemUser.setCreatedAt(LocalDateTime.now());
        systemUser.setCreatedBy("system");
        userRepository.save(systemUser);
    }

    // helper to create and persist DefaultData with required id and audit fields
    private DefaultData saveDefaultData(DataCategory category, String name, String description) {
        DefaultData d = DefaultData.builder()
                .dataCategory(category)
                .dataName(name)
                .description(description)
                .isActive(true)
                .build();
        d.setId(UUID.randomUUID());
        d.setCreatedAt(LocalDateTime.now());
        d.setCreatedBy("system");
        return defaultDataRepository.save(d);
    }

    @Test
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void getDefaultDataPage_asSystem_returnsPage() throws Exception {
        // seed couple of default data
        DefaultData d1 = saveDefaultData(DataCategory.ROLE, "ADMIN", "admin role");
        DefaultData d2 = saveDefaultData(DataCategory.ROLE, "USER", "user role");

        mockMvc.perform(get("/api/default-data/page").header("X-Application-Name","user-service").param("page","0").param("size","10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void getDefaultDataById_asSystem_returnsOk() throws Exception {
        DefaultData d = saveDefaultData(DataCategory.ROLE, "ADMIN", "admin");

        mockMvc.perform(get("/api/default-data/" + d.getId()).header("X-Application-Name","user-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dataName").value("ADMIN"));
    }

    @Test
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void getDefaultDataByCategory_asSystem_returnsPage() throws Exception {
        DefaultData d = saveDefaultData(DataCategory.ROLE, "ADMIN", "admin");

        mockMvc.perform(get("/api/default-data/page/category/ROLE").header("X-Application-Name","user-service").param("page","0").param("size","10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].dataName").value("ADMIN"));
    }

    @Test
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void createUpdateDeleteDefaultData_asSystem_lifecycle() throws Exception {
        // create
        DefaultDataCreateRequest createReq = new DefaultDataCreateRequest();
        createReq.setDataCategory("ROLE");
        createReq.setName("TEST_NAME");
        createReq.setDescription("test desc");

        var createResult = mockMvc.perform(post("/api/default-data")
                        .header("X-Application-Name","user-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.dataName").value("TEST_NAME"))
                .andReturn();

        // extract id from response
        String body = createResult.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(body);
        String id = root.path("data").path("id").asText();
        assertThat(id).isNotBlank();

        // update
        DefaultDataUpdateRequest updateReq = new DefaultDataUpdateRequest();
        updateReq.setDescription("updated-desc");
        mockMvc.perform(put("/api/default-data/" + id)
                        .header("X-Application-Name","user-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.description").value("updated-desc"));

        // delete
        mockMvc.perform(delete("/api/default-data/" + id)
                        .header("X-Application-Name","user-service"))
                .andExpect(status().isNoContent());

    }
}
