package com.quetoquenana.personservice.security;

import com.quetoquenana.personservice.config.SecurityConfig;
import com.quetoquenana.personservice.controller.ProfileController;
import com.quetoquenana.personservice.service.ProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@Import(SecurityConfig.class)
class ProfileControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfileService profileService;

    private static final String PROFILE_PAYLOAD = "{\"gender\":\"M\"}";
    private static final String PERSON_ID = "00000000-0000-0000-0000-000000000000";
    private static final String PROFILE_ID = "00000000-0000-0000-0000-000000000001";

    @Test
    @DisplayName("POST /api/persons/{id}/profile returns 401 when unauthenticated")
    void addProfile_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(post("/api/persons/" + PERSON_ID + "/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PROFILE_PAYLOAD))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/persons/profile/{id} returns 401 when unauthenticated")
    void updateProfile_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(put("/api/persons/profile/" + PROFILE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(PROFILE_PAYLOAD))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/persons/{id}/profile returns 403 for USER role")
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void addProfile_UserRole_Returns403() throws Exception {
        mockMvc.perform(post("/api/persons/" + PERSON_ID + "/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PROFILE_PAYLOAD))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/persons/profile/{id} returns 403 for USER role")
    @WithMockUser(username = "system", roles = {"SYSTEM"})
    void updateProfile_UserRole_Returns403() throws Exception {
        mockMvc.perform(put("/api/persons/profile/" + PROFILE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(PROFILE_PAYLOAD))
                .andExpect(status().isForbidden());
    }
}

