package com.quetoquenana.personservice.security;

import com.quetoquenana.personservice.config.SecurityConfig;
import com.quetoquenana.personservice.controller.AddressController;
import com.quetoquenana.personservice.service.AddressService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AddressController.class)
@Import(SecurityConfig.class)
class AddressControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AddressService addressService;

    private static final String ADDRESS_PAYLOAD = "{\"number\":\"123456789\"}";
    private static final String PERSON_ID = "00000000-0000-0000-0000-000000000000";
    private static final String ADDRESS_ID = "00000000-0000-0000-0000-000000000001";

    @Test
    @DisplayName("POST /api/persons/{id}/address returns 401 when unauthenticated")
    void addAddress_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(post("/api/persons/" + PERSON_ID + "/address")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ADDRESS_PAYLOAD))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/persons/address/{id} returns 401 when unauthenticated")
    void updateAddress_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(put("/api/persons/address/" + ADDRESS_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ADDRESS_PAYLOAD))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/persons/{id} returns 401 when unauthenticated")
    void deleteAddress_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(delete("/api/persons/" + ADDRESS_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    @DisplayName("POST /api/persons/{id}/address returns 200 for USER role")
    void addAddress_UserRole_Returns200() throws Exception {
        mockMvc.perform(post("/api/persons/" + PERSON_ID + "/address")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ADDRESS_PAYLOAD))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/persons/{id}/address returns 200 for ADMIN role")
    void addAddress_AdminRole_Returns200() throws Exception {
        mockMvc.perform(post("/api/persons/" + PERSON_ID + "/address")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ADDRESS_PAYLOAD))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    @DisplayName("PUT /api/persons/address/{id} returns 200 for USER role")
    void updateAddress_UserRole_Returns200() throws Exception {
        mockMvc.perform(put("/api/persons/address/" + ADDRESS_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ADDRESS_PAYLOAD))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("PUT /api/persons/address/{id} returns 200 for ADMIN role")
    void updateAddress_AdminRole_Returns200() throws Exception {
        mockMvc.perform(put("/api/persons/address/" + ADDRESS_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ADDRESS_PAYLOAD))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    @DisplayName("DELETE /api/persons/address/{id} returns 204 for USER role")
    void deleteAddress_UserRole_Returns204() throws Exception {
        mockMvc.perform(delete("/api/persons/address/" + ADDRESS_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("DELETE /api/persons/address/{id} returns 204 for ADMIN role")
    void deleteAddress_AdminRole_Returns204() throws Exception {
        mockMvc.perform(delete("/api/persons/address/" + ADDRESS_ID))
                .andExpect(status().isNoContent());
    }
}

