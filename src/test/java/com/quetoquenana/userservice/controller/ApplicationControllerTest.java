package com.quetoquenana.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.quetoquenana.userservice.dto.*;
import com.quetoquenana.userservice.exception.DuplicateRecordException;
import com.quetoquenana.userservice.exception.RecordNotFoundException;
import com.quetoquenana.userservice.model.ApiResponse;
import com.quetoquenana.userservice.model.Application;
import com.quetoquenana.userservice.model.AppRole;
import com.quetoquenana.userservice.model.AppRoleUser;
import com.quetoquenana.userservice.service.ApplicationService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class ApplicationControllerTest {

    @Mock
    private ApplicationService applicationService;

    @InjectMocks
    private ApplicationController applicationController;

    private Application application;
    private UUID appId;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        appId = UUID.randomUUID();
        application = Application.builder()
                .id(appId)
                .name("my-app")
                .description("A test application")
                .active(true)
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void testGetApplicationById_NotFound() {
        when(applicationService.findById(appId)).thenReturn(Optional.empty());
        assertThrows(RecordNotFoundException.class, () -> applicationController.getApplicationById(appId));
    }

    @Test
    void testGetApplicationById_Found() throws Exception {
        when(applicationService.findById(appId)).thenReturn(Optional.of(application));
        ResponseEntity<ApiResponse> response = applicationController.getApplicationById(appId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);

        Map<String, Object> dataMap = (Map<String, Object>) apiResponse.getData();
        assertNotNull(dataMap);
        Application data = (Application) dataMap.get("application");
        assertEquals(application, data);

        String json = objectMapper.writerWithView(Application.ApplicationDetail.class).writeValueAsString(data);
        assertTrue(json.contains("name"));
        assertTrue(json.contains("description"));
        assertTrue(json.contains("active"));
    }

    @Test
    void testCreateApplication_ReturnsCreated() {
        ApplicationCreateRequest createRequest = new ApplicationCreateRequest();
        createRequest.setName("my-app");
        createRequest.setDescription("A test application");
        createRequest.setIsActive(true);

        when(applicationService.save(any(ApplicationCreateRequest.class))).thenReturn(application);

        ResponseEntity<ApiResponse> response = applicationController.createApplication(createRequest);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);

        @SuppressWarnings("unchecked")
        Map<String, Object> dataMap = (Map<String, Object>) apiResponse.getData();
        assertNotNull(dataMap);
        Application returned = (Application) dataMap.get("application");
        assertEquals(application, returned);
    }

    @Test
    void testCreateApplication_Duplicate() {
        ApplicationCreateRequest createRequest = new ApplicationCreateRequest();
        createRequest.setName("my-app");
        when(applicationService.save(any(ApplicationCreateRequest.class)))
                .thenThrow(new DuplicateRecordException("application.name.duplicate"));

        assertThrows(DuplicateRecordException.class, () -> applicationController.createApplication(createRequest));
    }

    @Test
    void testUpdateApplication_ReturnsUpdated() {
        ApplicationUpdateRequest updateRequest = new ApplicationUpdateRequest();
        updateRequest.setName("my-app");
        updateRequest.setDescription("Updated");
        updateRequest.setIsActive(false);

        when(applicationService.update(appId, updateRequest)).thenReturn(application);

        ResponseEntity<ApiResponse> response = applicationController.updateApplication(appId, updateRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);

        @SuppressWarnings("unchecked")
        Map<String, Object> dataMap = (Map<String, Object>) apiResponse.getData();
        assertNotNull(dataMap);
        Application data = (Application) dataMap.get("application");
        assertEquals(application, data);
    }

    @Test
    void testUpdateApplication_NotFound() {
        ApplicationUpdateRequest updateRequest = new ApplicationUpdateRequest();
        updateRequest.setName("my-app");
        when(applicationService.update(appId, updateRequest)).thenThrow(new RecordNotFoundException());
        assertThrows(RecordNotFoundException.class, () -> applicationController.updateApplication(appId, updateRequest));
    }

    @Test
    void testDeleteApplication_Success() {
        doNothing().when(applicationService).deleteById(appId);
        ResponseEntity<Void> response = applicationController.deleteApplication(appId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testDeleteApplication_NotFound() {
        // simulate service throwing when not found
        org.mockito.Mockito.doThrow(new RecordNotFoundException()).when(applicationService).deleteById(appId);
        assertThrows(RecordNotFoundException.class, () -> applicationController.deleteApplication(appId));
    }

    @Test
    void testGetAllApplicationsPage_returnsOk() {
        var page = new PageImpl<>(List.of(application));
        when(applicationService.findAll(any(Pageable.class))).thenReturn(page);

        ResponseEntity<ApiResponse> response = applicationController.getAllApplicationsPage(0, 10);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertNotNull(apiResponse.getData());
    }

    @Test
    void testSearchApplications_returnsOk() {
        var page = new PageImpl<>(List.of(application));
        when(applicationService.searchByName(eq("my"), any(Pageable.class))).thenReturn(page);

        ResponseEntity<ApiResponse> response = applicationController.searchApplications("my", 0, 10);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertNotNull(apiResponse.getData());
    }

    @Test
    void testAddRole_ReturnsCreated() {
        AppRoleCreateRequest req = new AppRoleCreateRequest();
        req.setRoleName("ADMIN");
        AppRole role = AppRole.builder().id(UUID.randomUUID()).roleName("ADMIN").build();
        when(applicationService.addRole(eq(appId), any(AppRoleCreateRequest.class))).thenReturn(role);

        ResponseEntity<ApiResponse> response = applicationController.addRole(appId, req);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        @SuppressWarnings("unchecked")
        Map<String, Object> dataMap = (Map<String, Object>) apiResponse.getData();
        assertEquals(role, dataMap.get("appRole"));
    }

    @Test
    void testAddUser_ReturnsCreated() {
        AppRoleUserCreateRequest req = getAppRoleUserCreateRequest();

        AppRoleUser aru = AppRoleUser.builder().id(UUID.randomUUID()).build();
        when(applicationService.addUser(eq(appId), any(AppRoleUserCreateRequest.class))).thenReturn(aru);

        ResponseEntity<ApiResponse> response = applicationController.addUser(appId, req);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        @SuppressWarnings("unchecked")
        Map<String, Object> dataMap = (Map<String, Object>) apiResponse.getData();
        assertEquals(aru, dataMap.get("appRoleUser"));
    }

    private static @NotNull AppRoleUserCreateRequest getAppRoleUserCreateRequest() {
        AppRoleUserCreateRequest appRoleUserCreateRequest = new AppRoleUserCreateRequest();
        // construct nested user request
        UserCreateRequest userCreateRequest = new UserCreateRequest();
        userCreateRequest.setUsername("u@example.com");
        PersonCreateRequest personCreateRequest = new PersonCreateRequest();
        personCreateRequest.setIdNumber("ID1");
        userCreateRequest.setPerson(personCreateRequest);
        appRoleUserCreateRequest.setUser(userCreateRequest);
        appRoleUserCreateRequest.setRoleName("ADMIN");
        return appRoleUserCreateRequest;
    }

    @Test
    void testDeleteUser_Success() {
        doNothing().when(applicationService).removeUser(appId, "user");
        ResponseEntity<Void> response = applicationController.deleteUser(appId, "user");
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testDeleteUser_NotFound() {
        org.mockito.Mockito.doThrow(new RecordNotFoundException()).when(applicationService).removeUser(appId, "user");
        assertThrows(RecordNotFoundException.class, () -> applicationController.deleteUser(appId, "user"));
    }

    @Test
    void testDeleteRole_Success() {
        UUID roleId = UUID.randomUUID();
        doNothing().when(applicationService).deleteRole(appId, roleId);
        ResponseEntity<Void> response = applicationController.deleteRole(appId, roleId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testDeleteRole_NotFound() {
        UUID roleId = UUID.randomUUID();
        org.mockito.Mockito.doThrow(new RecordNotFoundException()).when(applicationService).deleteRole(appId, roleId);
        assertThrows(RecordNotFoundException.class, () -> applicationController.deleteRole(appId, roleId));
    }
}
