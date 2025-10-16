package com.quetoquenana.personservice.security;

import com.quetoquenana.personservice.config.SecurityConfig;
import com.quetoquenana.personservice.controller.PhoneController;
import com.quetoquenana.personservice.service.PhoneService;
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

@WebMvcTest(PhoneController.class)
@Import(SecurityConfig.class)
class PhoneControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PhoneService phoneService;

    private static final String PHONE_PAYLOAD = "{\"number\":\"123456789\"}";
    private static final String PERSON_ID = "00000000-0000-0000-0000-000000000000";
    private static final String PHONE_ID = "00000000-0000-0000-0000-000000000001";

    @Test
    @DisplayName("POST /api/persons/{id}/phone returns 401 when unauthenticated")
    void addPhone_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(post("/api/persons/" + PERSON_ID + "/phone")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PHONE_PAYLOAD))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/persons/phone/{id} returns 401 when unauthenticated")
    void updatePhone_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(put("/api/persons/phone/" + PHONE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(PHONE_PAYLOAD))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/persons/{id} returns 401 when unauthenticated")
    void deletePhone_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(delete("/api/persons/" + PHONE_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    @DisplayName("POST /api/persons/{id}/phone returns 200 for USER role")
    void addPhone_UserRole_Returns200() throws Exception {
        mockMvc.perform(post("/api/persons/" + PERSON_ID + "/phone")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PHONE_PAYLOAD))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/persons/{id}/phone returns 200 for ADMIN role")
    void addPhone_AdminRole_Returns200() throws Exception {
        mockMvc.perform(post("/api/persons/" + PERSON_ID + "/phone")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PHONE_PAYLOAD))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    @DisplayName("PUT /api/persons/phone/{id} returns 200 for USER role")
    void updatePhone_UserRole_Returns200() throws Exception {
        mockMvc.perform(put("/api/persons/phone/" + PHONE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(PHONE_PAYLOAD))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("PUT /api/persons/phone/{id} returns 200 for ADMIN role")
    void updatePhone_AdminRole_Returns200() throws Exception {
        mockMvc.perform(put("/api/persons/phone/" + PHONE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(PHONE_PAYLOAD))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    @DisplayName("DELETE /api/persons/phone/{id} returns 204 for USER role")
    void deletePhone_UserRole_Returns204() throws Exception {
        mockMvc.perform(delete("/api/persons/phone/" + PHONE_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("DELETE /api/persons/phone/{id} returns 204 for ADMIN role")
    void deletePhone_AdminRole_Returns204() throws Exception {
        mockMvc.perform(delete("/api/persons/phone/" + PHONE_ID))
                .andExpect(status().isNoContent());
    }
}

