package com.quetoquenana.personservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.quetoquenana.personservice.model.ApiBaseResponseView;
import com.quetoquenana.personservice.model.ApiResponse;
import com.quetoquenana.personservice.model.Execution;
import com.quetoquenana.personservice.service.ExecutionService;
import com.quetoquenana.personservice.util.JsonViewPageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class ExecutionControllerTest {
    @Mock
    private ExecutionService executionService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private ExecutionController executionController;

    private Execution execution;
    private UUID executionId;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        executionId = UUID.randomUUID();
        execution = new Execution(
            executionId,
            LocalDateTime.now(),
            "server1",
            "127.0.0.1",
            "1.0.0",
            "dev"
        );
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void testGetAllExecutions_ReturnsList() throws Exception {
        // Given: a list with one execution returned by the service
        List<Execution> executions = Collections.singletonList(execution);
        when(executionService.findAll()).thenReturn(executions);

        // When: the controller's getAllExecutions is called
        ResponseEntity<ApiResponse> response = executionController.getAllExecutions();

        // Then: the response should be 200 OK and contain the executions list in ApiResponse.data
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertInstanceOf(List.class, response.getBody().getData());
        List<?> resultList = (List<?>) response.getBody().getData();
        assertFalse(resultList.isEmpty());
        assertInstanceOf(Execution.class, resultList.getFirst());
        // And: the JSON payload should include only the fields for ExecutionList view
        String json = objectMapper.writerWithView(Execution.ExecutionList.class).writeValueAsString(resultList);
        assertTrue(json.contains("id"));
        assertTrue(json.contains("executedAt"));
        assertTrue(json.contains("appVersion"));
        assertTrue(json.contains("environment"));
        assertFalse(json.contains("serverName"));
        assertFalse(json.contains("ipAddress"));
    }

    @Test
    void testGetExecutionById_Found() throws Exception {
        // Given: the service returns a valid execution for the given id
        when(executionService.findById(executionId)).thenReturn(Optional.ofNullable(execution));

        // When: the controller's getExecutionById is called
        ResponseEntity<ApiResponse> response = executionController.getExecutionById(executionId);

        // Then: the response should be 200 OK and contain the execution in ApiResponse.data
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertInstanceOf(Execution.class, response.getBody().getData());
        Execution result = (Execution) response.getBody().getData();
        // And: the JSON payload should include all fields for ExecutionDetail view
        String json = objectMapper.writerWithView(Execution.ExecutionDetail.class).writeValueAsString(result);
        assertTrue(json.contains("id"));
        assertTrue(json.contains("executedAt"));
        assertTrue(json.contains("appVersion"));
        assertTrue(json.contains("environment"));
        assertTrue(json.contains("serverName"));
        assertTrue(json.contains("ipAddress"));
    }

    @Test
    void testGetExecutionById_NotFound_English() {
        when(executionService.findById(executionId)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("error.record.not.found"), any(), eq(Locale.ENGLISH))).thenReturn("Resource not found.");
        assertThrows(com.quetoquenana.personservice.exception.RecordNotFoundException.class, () -> executionController.getExecutionById(executionId));
    }

    @Test
    void testGetExecutionById_NotFound_Spanish() {
        when(executionService.findById(executionId)).thenReturn(Optional.empty());
        Locale spanish = Locale.forLanguageTag("es");
        when(messageSource.getMessage(eq("error.record.not.found"), any(), eq(spanish))).thenReturn("Recurso no encontrado.");
        assertThrows(com.quetoquenana.personservice.exception.RecordNotFoundException.class, () -> executionController.getExecutionById(executionId));
    }

    @Test
    void testGetExecutionsPage_ReturnsJsonViewPageUtil() throws Exception {
        // Given: a page with one execution returned by the service
        Page<Execution> page = new PageImpl<>(Collections.singletonList(execution), PageRequest.of(0, 10), 1);
        when(executionService.findAll(PageRequest.of(0, 10))).thenReturn(page);

        // When: the controller's getExecutionsPage is called
        ResponseEntity<ApiResponse> response = executionController.getExecutionsPage(0, 10);

        // Then: the response should be 200 OK and contain a JsonViewPageUtil in ApiResponse.data
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertInstanceOf(JsonViewPageUtil.class, response.getBody().getData());
        JsonViewPageUtil<?> pageUtil = (JsonViewPageUtil<?>) response.getBody().getData();
        // Serialize only the list of Execution objects with the view
        String json = objectMapper.writerWithView(Execution.ExecutionList.class)
                .writeValueAsString(pageUtil.getContent());
        assertTrue(json.contains("id"));
        assertTrue(json.contains("executedAt"));
        assertTrue(json.contains("appVersion"));
        assertTrue(json.contains("environment"));
        assertFalse(json.contains("serverName"));
        assertFalse(json.contains("ipAddress"));
        // Also check pagination metadata
        String metaJson = objectMapper.writerWithView(ApiBaseResponseView.Always.class)
                .writeValueAsString(pageUtil);
        assertTrue(metaJson.contains("totalPages"));
        assertTrue(metaJson.contains("totalElements"));
    }
}
