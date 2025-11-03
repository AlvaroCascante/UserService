package com.quetoquenana.userservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.userservice.dto.ApplicationCreateRequest;
import com.quetoquenana.userservice.dto.ApplicationUpdateRequest;
import com.quetoquenana.userservice.exception.RecordNotFoundException;
import com.quetoquenana.userservice.model.ApiResponse;
import com.quetoquenana.userservice.model.Application;
import com.quetoquenana.userservice.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Slf4j
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @JsonView(Application.ApplicationList.class)
    public ResponseEntity<ApiResponse> getAllApplications() {
        log.info("GET /api/applications called");
        List<Application> entities = applicationService.findAll();
        return ResponseEntity.ok(new ApiResponse(entities));
    }

    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    @JsonView(Application.ApplicationList.class)
    public ResponseEntity<ApiResponse> getAllApplicationsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("GET /api/applications/page called with page={}, size={}", page, size);
        Page<Application> entities = applicationService.findAll(PageRequest.of(page, size));
        return ResponseEntity.ok(new ApiResponse(new com.quetoquenana.userservice.util.JsonViewPageUtil<>(entities, entities.getPageable())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @JsonView(Application.ApplicationDetail.class)
    public ResponseEntity<ApiResponse> getApplicationById(@PathVariable UUID id) {
        log.info("GET /api/applications/{} called", id);
        return applicationService.findById(id)
                .map(entity -> ResponseEntity.ok(new ApiResponse(entity)))
                .orElseGet(() -> {
                    log.error("Application with id {} not found", id);
                    throw new RecordNotFoundException();
                });
    }

    @GetMapping("/name/{name}")
    @PreAuthorize("hasRole('ADMIN')")
    @JsonView(Application.ApplicationDetail.class)
    public ResponseEntity<ApiResponse> getApplicationByName(@PathVariable String name) {
        log.info("GET /api/applications/name/{} called", name);
        return applicationService.findByName(name)
                .map(entity -> ResponseEntity.ok(new ApiResponse(entity)))
                .orElseGet(() -> {
                    log.error("Application with name {} not found", name);
                    throw new RecordNotFoundException();
                });
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @JsonView(Application.ApplicationList.class)
    public ResponseEntity<ApiResponse> searchApplications(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("GET /api/applications/search called with name={}, page={}, size={}", name, page, size);
        org.springframework.data.domain.Page<Application> entities = applicationService.searchByName(name, org.springframework.data.domain.PageRequest.of(page, size));
        return ResponseEntity.ok(new ApiResponse(new com.quetoquenana.userservice.util.JsonViewPageUtil<>(entities, entities.getPageable())));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @JsonView(Application.ApplicationDetail.class)
    public ResponseEntity<ApiResponse> createApplication(@Valid @RequestBody ApplicationCreateRequest request) {
        log.info("POST /api/applications called with payload: {}", request);
        Application entity = applicationService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(entity));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @JsonView(Application.ApplicationDetail.class)
    public ResponseEntity<ApiResponse> updateApplication(@PathVariable UUID id, @Valid @RequestBody ApplicationUpdateRequest request) {
        log.info("PUT /api/applications/{} called with payload: {}", id, request);
        Application entity = applicationService.update(id, request);
        return ResponseEntity.ok(new ApiResponse(entity));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteApplication(@PathVariable UUID id) {
        log.info("DELETE /api/applications/{} called", id);
        applicationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
