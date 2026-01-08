package com.quetoquenana.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quetoquenana.userservice.dto.AppRoleCreateRequest;
import com.quetoquenana.userservice.dto.AppRoleUserCreateRequest;
import com.quetoquenana.userservice.dto.ApplicationCreateRequest;
import com.quetoquenana.userservice.dto.ApplicationUpdateRequest;
import com.quetoquenana.userservice.model.Application;
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
class ApplicationControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private AppRoleRepository appRoleRepository;
    @Autowired
    private AppRoleUserRepository appRoleUserRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        appRoleUserRepository.deleteAll();
        appRoleRepository.deleteAll();
        applicationRepository.deleteAll();
        userRepository.deleteAll();
        personRepository.deleteAll();
        // ensure a current user (principal) exists for service methods that look up current user
        var person = TestEntityFactory.createPerson();
        person = personRepository.save(person);
        // create a system user directly so lookups succeed
        var systemUser = com.quetoquenana.userservice.model.User.builder()
                .username("system")
                .passwordHash(passwordEncoder.encode("password"))
                .person(person)
                .nickname("system")
                .userStatus(UserStatus.ACTIVE)
                .build();
        systemUser.setCreatedAt(LocalDateTime.now());
        systemUser.setCreatedBy("test");
        userRepository.save(systemUser);
    }

    @Test
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void createApplication_asSystem_returnsCreated() throws Exception {
        ApplicationCreateRequest req = new ApplicationCreateRequest();
        req.setName("integ-app-create");
        req.setDescription("integration create");
        req.setIsActive(true);

        // sanity check: ensure the system user was seeded and is visible to the repository
        assertThat(userRepository.findByUsernameIgnoreCase("system")).isPresent();

        mockMvc.perform(post("/api/applications")
                        .header("X-Application-Name", "user-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.application.name").value("integ-app-create"));

        assertThat(applicationRepository.existsByNameIgnoreCase("integ-app-create")).isTrue();
    }

    @Test
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void getApplicationById_asSystem_returnsOk() throws Exception {
        Application app = Application.builder()
                .name("get-by-id-app")
                .description("d")
                .active(true)
                .build();
        app.setCreatedAt(LocalDateTime.now());
        app.setCreatedBy("test");
        app = applicationRepository.save(app);

        mockMvc.perform(get("/api/applications/" + app.getId())
                        .header("X-Application-Name", "user-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.application.name").value("get-by-id-app"));
    }

    @Test
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void updateApplication_asSystem_returnsOk() throws Exception {
        Application app = Application.builder()
                .name("to-update-app")
                .description("d")
                .active(true)
                .build();
        app.setCreatedAt(LocalDateTime.now());
        app.setCreatedBy("test");
        app = applicationRepository.save(app);

        ApplicationUpdateRequest update = new ApplicationUpdateRequest();
        update.setName("updated-name");
        update.setDescription("updated-desc");
        update.setIsActive(false);

        mockMvc.perform(put("/api/applications/" + app.getId())
                        .header("X-Application-Name", "user-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.application.description").value("updated-desc"))
                .andExpect(jsonPath("$.data.application.active").value(false));

        Application updated = applicationRepository.findById(app.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("updated-name");
        assertThat(updated.getDescription()).isEqualTo("updated-desc");
        assertThat(updated.isActive()).isFalse();
    }

    @Test
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void deleteApplication_asSystem_setsInactive() throws Exception {
        Application app = Application.builder()
                .name("to-delete-app")
                .description("d")
                .active(true)
                .build();
        app.setCreatedAt(LocalDateTime.now());
        app.setCreatedBy("test");
        app = applicationRepository.save(app);

        mockMvc.perform(delete("/api/applications/" + app.getId())
                        .header("X-Application-Name", "user-service"))
                .andExpect(status().isNoContent());

        Application after = applicationRepository.findById(app.getId()).orElseThrow();
        assertThat(after.isActive()).isFalse();
    }

    @Test
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void getAllApplicationsPage_asSystem_returnsPage() throws Exception {
        Application a1 = Application.builder().name("p1-app").description("d1").active(true).build();
        a1.setCreatedAt(LocalDateTime.now());
        a1.setCreatedBy("test");
        Application a2 = Application.builder().name("p2-app").description("d2").active(true).build();
        a2.setCreatedAt(LocalDateTime.now());
        a2.setCreatedBy("test");
        applicationRepository.save(a1);
        applicationRepository.save(a2);

        mockMvc.perform(get("/api/applications/page")
                        .header("X-Application-Name", "user-service")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void searchApplications_asSystem_returnsPage() throws Exception {
        Application app = Application.builder().name("find-me-app").description("d").active(true).build();
        app.setCreatedAt(LocalDateTime.now());
        app.setCreatedBy("test");
        applicationRepository.save(app);

        mockMvc.perform(get("/api/applications/search")
                        .header("X-Application-Name", "user-service")
                        .param("name", "find-me")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].name").value("find-me-app"));
    }

    @Test
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void addRole_asSystem_returnsCreated() throws Exception {
        Application app = Application.builder().name("with-role-app").description("d").active(true).build();
        app.setCreatedAt(LocalDateTime.now());
        app.setCreatedBy("test");
        app = applicationRepository.save(app);

        AppRoleCreateRequest req = new AppRoleCreateRequest();
        req.setRoleName("ADMIN");

        mockMvc.perform(post("/api/applications/" + app.getId() + "/role")
                        .header("X-Application-Name", "user-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.appRole.roleName").value("ADMIN"));

        assertThat(appRoleRepository.findAll()).isNotEmpty();
    }

    @Test
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void addUser_asSystem_returnsCreated() throws Exception {
        Application app = Application.builder().name("app-for-user").description("d").active(true).build();
        app.setCreatedAt(LocalDateTime.now());
        app.setCreatedBy("test");
        app = applicationRepository.save(app);

        // create role first so addUser can find it
        AppRoleCreateRequest roleReq = new AppRoleCreateRequest();
        roleReq.setRoleName("USER");
        mockMvc.perform(post("/api/applications/" + app.getId() + "/role")
                        .header("X-Application-Name", "user-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleReq)))
                .andExpect(status().isCreated());

        AppRoleUserCreateRequest req = new AppRoleUserCreateRequest();
        req.setRoleName("USER");
        var userReq = new com.quetoquenana.userservice.dto.UserCreateRequest();
        userReq.setUsername("integ-u-" + UUID.randomUUID() + "@example.com");
        var personReq = new com.quetoquenana.userservice.dto.PersonCreateRequest();
        personReq.setIdNumber("ID-" + UUID.randomUUID());
        personReq.setName("Integ");
        personReq.setLastname("User");
        userReq.setPerson(personReq);
        req.setUser(userReq);

        mockMvc.perform(post("/api/applications/" + app.getId() + "/user")
                        .header("X-Application-Name", "user-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.appRoleUser.id").exists());

        assertThat(appRoleUserRepository.findAll()).isNotEmpty();
    }

    @Test
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void deleteUser_asSystem_noContent() throws Exception {
        Application app = Application.builder().name("app-for-delete").description("d").active(true).build();
        app.setCreatedAt(LocalDateTime.now());
        app.setCreatedBy("test");
        app = applicationRepository.save(app);

        // create role
        AppRoleCreateRequest roleReq = new AppRoleCreateRequest();
        roleReq.setRoleName("USER");
        mockMvc.perform(post("/api/applications/" + app.getId() + "/role")
                        .header("X-Application-Name", "user-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleReq)))
                .andExpect(status().isCreated());

        AppRoleUserCreateRequest createReq = new AppRoleUserCreateRequest();
        createReq.setRoleName("USER");
        var userReq = new com.quetoquenana.userservice.dto.UserCreateRequest();
        String username = "del-user-" + UUID.randomUUID() + "@example.com";
        userReq.setUsername(username);
        var personReq = new com.quetoquenana.userservice.dto.PersonCreateRequest();
        personReq.setIdNumber("DEL-" + UUID.randomUUID());
        personReq.setName("Integ");
        personReq.setLastname("User");
        userReq.setPerson(personReq);
        createReq.setUser(userReq);

        mockMvc.perform(post("/api/applications/" + app.getId() + "/user")
                        .header("X-Application-Name", "user-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/applications/" + app.getId() + "/user/" + username)
                        .header("X-Application-Name", "user-service"))
                .andExpect(status().isNoContent());

        assertThat(appRoleUserRepository.findAll()).isEmpty();
    }

    @Test
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void deleteRole_asSystem_noContent() throws Exception {
        Application app = Application.builder().name("app-role-del").description("d").active(true).build();
        app.setCreatedAt(LocalDateTime.now());
        app.setCreatedBy("test");
        app = applicationRepository.save(app);

        // create role directly and persist
        var role = com.quetoquenana.userservice.model.AppRole.builder()
                .roleName("TO_DELETE")
                .application(app)
                .build();
        role.setCreatedAt(LocalDateTime.now());
        role.setCreatedBy("test");
        appRoleRepository.save(role);

        mockMvc.perform(delete("/api/applications/" + app.getId() + "/role/" + role.getId())
                        .header("X-Application-Name", "user-service"))
                .andExpect(status().isNoContent());

        assertThat(appRoleRepository.findById(role.getId())).isEmpty();
    }
}
