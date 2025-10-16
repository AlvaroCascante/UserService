package com.quetoquenana.personservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.personservice.exception.RecordNotFoundException;
import com.quetoquenana.personservice.model.ApiResponse;
import com.quetoquenana.personservice.model.Execution;
import com.quetoquenana.personservice.service.ExecutionService;
import com.quetoquenana.personservice.util.JsonViewPageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/executions")
@RequiredArgsConstructor
@Slf4j
public class ExecutionController {

    private final ExecutionService executionService;

    /**
     * Get all executions
     * @return ResponseEntity with ApiResponse (list of executions)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Only ADMIN role can access
    @JsonView(Execution.ExecutionList.class)
    public ResponseEntity<ApiResponse> getAllExecutions() {
        log.info("GET /api/executions called");
        List<Execution> entities = executionService.findAll();
        return ResponseEntity.ok(new ApiResponse(entities));
    }

    /**
     * Get execution by id
     * @param id UUID of execution
     * @return ResponseEntity with ApiResponse (execution or error)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Only ADMIN role can access
    @JsonView(Execution.ExecutionDetail.class)
    public ResponseEntity<ApiResponse> getExecutionById(
            @PathVariable UUID id
    ) {
        log.info("GET /api/executions/{} called", id);
        return executionService.findById(id)
            .map(entity -> ResponseEntity.ok(new ApiResponse(entity)))
            .orElseGet(() -> {
                log.error("Execution with id {} not found", id);
                throw new RecordNotFoundException();
            });
    }

    /**
     * Get executions with pagination
     * @param page page number (default 0)
     * @param size page size (default 10)
     * @return ResponseEntity with ApiResponse (paginated executions)
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')") // Only ADMIN role can access
    @JsonView(Execution.ExecutionList.class)
    public ResponseEntity<ApiResponse> getExecutionsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/executions/page?page={}&size={} called", page, size);
        Page<Execution> entities = executionService.findAll(PageRequest.of(page, size));
        return ResponseEntity.ok(new ApiResponse(new JsonViewPageUtil<>(entities, entities.getPageable())));
    }
}