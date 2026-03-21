package com.quetoquenana.userservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.userservice.dto.AppRoleCreateRequest;
import com.quetoquenana.userservice.dto.AppRoleUserCreateRequest;
import com.quetoquenana.userservice.dto.ApplicationCreateRequest;
import com.quetoquenana.userservice.dto.ApplicationUpdateRequest;
import com.quetoquenana.userservice.exception.RecordNotFoundException;
import com.quetoquenana.userservice.dto.ApiResponse;
import com.quetoquenana.userservice.model.AppRole;
import com.quetoquenana.userservice.model.AppRoleUser;
import com.quetoquenana.userservice.model.Application;
import com.quetoquenana.userservice.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.UUID;

import static com.quetoquenana.userservice.util.Constants.Pagination.*;
import static com.quetoquenana.userservice.util.Constants.Roles.ROLE_NAME_ADMIN;
import static com.quetoquenana.userservice.util.Constants.Roles.ROLE_NAME_USER;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Slf4j
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping("/page")
    @PreAuthorize("hasRole('SYSTEM')")
    @JsonView(Application.ApplicationList.class)
    public ResponseEntity<ApiResponse> getAllApplicationsPage(
            @RequestParam(defaultValue = PAGE) int page,
            @RequestParam(defaultValue = PAGE_SIZE) int size
    ) {
        log.info("GET /api/applications/page called with page={}, size={}", page, size);
        Page<Application> entities = applicationService.findAll(PageRequest.of(page, size));
        return ResponseEntity.ok(new ApiResponse(new com.quetoquenana.userservice.util.JsonViewPageUtil<>(entities, entities.getPageable())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM')")
    @JsonView(Application.ApplicationDetail.class)
    public ResponseEntity<ApiResponse> getApplicationById(@PathVariable UUID id) {
        log.info("GET /api/applications/{} called", id);
        return applicationService.findById(id)
                .map(entity -> ResponseEntity.ok(new ApiResponse(Collections.singletonMap("application", entity))))
                .orElseGet(() -> {
                    log.error("Application with id {} not found", id);
                    throw new RecordNotFoundException();
                });
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SYSTEM')")
    @JsonView(Application.ApplicationList.class)
    public ResponseEntity<ApiResponse> searchApplications(
            @RequestParam String name,
            @RequestParam(defaultValue = PAGE) int page,
            @RequestParam(defaultValue = PAGE_SIZE) int size
    ) {
        log.info("GET /api/applications/search called with name={}, page={}, size={}", name, page, size);
        Page<Application> entities = applicationService.searchByName(name, PageRequest.of(page, size));
        return ResponseEntity.ok(new ApiResponse(new com.quetoquenana.userservice.util.JsonViewPageUtil<>(entities, entities.getPageable())));
    }

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM')")
    @JsonView(Application.ApplicationDetail.class)
    public ResponseEntity<ApiResponse> createApplication(
            @Valid @RequestBody ApplicationCreateRequest request
    ) {
        log.info("POST /api/applications called with payload: {}", request);
        Application entity = applicationService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(Collections.singletonMap("application", entity)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM')")
    @JsonView(Application.ApplicationDetail.class)
    public ResponseEntity<ApiResponse> updateApplication(
            @PathVariable UUID id,
            @Valid @RequestBody ApplicationUpdateRequest request
    ) {
        log.info("PUT /api/applications/{} called with payload: {}", id, request);
        Application entity = applicationService.update(id, request);
        return ResponseEntity.ok(new ApiResponse(Collections.singletonMap("application", entity)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM')")
    public ResponseEntity<Void> deleteApplication(
            @PathVariable UUID id
    ) {
        log.info("DELETE /api/applications/{} called", id);
        applicationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/role")
    @JsonView(Application.ApplicationDetail.class)
    @PreAuthorize("hasRole('SYSTEM')")
    public ResponseEntity<ApiResponse> addRole(
            @PathVariable UUID id,
            @Valid @RequestBody AppRoleCreateRequest request
    ) {
        log.info("POST /api/applications/{}/role called with payload: {}", id, request);
        AppRole entity = applicationService.addRole(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(Collections.singletonMap("appRole", entity)));
    }

    @PostMapping("/{id}/user")
    @JsonView(Application.ApplicationDetail.class)
    @PreAuthorize("hasRole('SYSTEM') or hasRole('ADMIN')") // SYSTEM or ADMIN can add users to application
    public ResponseEntity<ApiResponse> addUser(
            @PathVariable UUID id,
            @Valid @RequestBody AppRoleUserCreateRequest request
    ) {
        log.info("POST /api/applications/{}/user called with payload: {}", id, request);
        AppRoleUser entity = applicationService.addUser(id, request, ROLE_NAME_ADMIN);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(Collections.singletonMap("appRoleUser", entity)));
    }

    @PostMapping("/{id}/user/customer")
    @JsonView(Application.ApplicationDetail.class)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> addUserCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody AppRoleUserCreateRequest request
    ) {
        log.info("POST /api/applications/{}/user/customer called with payload: {}", id, request);
        AppRoleUser entity = applicationService.addUser(id, request, ROLE_NAME_USER);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(Collections.singletonMap("appRoleUser", entity)));
    }

    @DeleteMapping("/{id}/user/{username}")
    @PreAuthorize("hasRole('SYSTEM')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID id,
            @PathVariable String username
    ) {
        log.info("DELETE /api/applications/{}/user/{} called", id, username);
        applicationService.removeUser(id, username);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/role/{roleId}")
    @PreAuthorize("hasRole('SYSTEM')")
    public ResponseEntity<Void> deleteRole(
            @PathVariable UUID id,
            @PathVariable UUID roleId
    ) {
        log.info("DELETE /api/applications/{}/role/{} called", id, roleId);
        applicationService.deleteRole(id, roleId);
        return ResponseEntity.noContent().build();
    }
}
