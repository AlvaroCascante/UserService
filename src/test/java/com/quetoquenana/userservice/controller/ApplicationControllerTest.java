package com.quetoquenana.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.quetoquenana.userservice.dto.ApplicationCreateRequest;
import com.quetoquenana.userservice.dto.ApplicationUpdateRequest;
import com.quetoquenana.userservice.exception.DuplicateRecordException;
import com.quetoquenana.userservice.exception.RecordNotFoundException;
import com.quetoquenana.userservice.model.ApiResponse;
import com.quetoquenana.userservice.model.Application;
import com.quetoquenana.userservice.service.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
                .isActive(true)
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
        Application data = (Application) apiResponse.getData();
        assertEquals(application, data);

        String json = objectMapper.writerWithView(Application.ApplicationDetail.class).writeValueAsString(data);
        assertTrue(json.contains("name"));
        assertTrue(json.contains("description"));
        assertTrue(json.contains("isActive"));
    }

    @Test
    void testGetApplicationByName_NotFound() {
        when(applicationService.findByName("unknown")).thenReturn(Optional.empty());
        assertThrows(RecordNotFoundException.class, () -> applicationController.getApplicationByName("unknown"));
    }

    @Test
    void testGetApplicationByName_Found() throws Exception {
        when(applicationService.findByName("my-app")).thenReturn(Optional.of(application));
        ResponseEntity<ApiResponse> response = applicationController.getApplicationByName("my-app");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        Application data = (Application) apiResponse.getData();
        assertEquals(application, data);
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
        Application data = (Application) apiResponse.getData();
        assertEquals(application, data);
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
        Application data = (Application) apiResponse.getData();
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
}
