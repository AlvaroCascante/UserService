package com.quetoquenana.userservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.userservice.dto.ResetPasswordRequest;
import com.quetoquenana.userservice.dto.UserCreateRequest;
import com.quetoquenana.userservice.dto.UserUpdateRequest;
import com.quetoquenana.userservice.exception.RecordNotFoundException;
import com.quetoquenana.userservice.model.ApiResponse;
import com.quetoquenana.userservice.model.User;
import com.quetoquenana.userservice.service.UserService;
import com.quetoquenana.userservice.util.JsonViewPageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping
    @JsonView(User.UserList.class)
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    public ResponseEntity<ApiResponse> getAllUsers() {
        log.info("GET /api/users called");
        List<User> entities = userService.findAll(PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        return ResponseEntity.ok(new ApiResponse(entities));
    }

    @GetMapping("/page")
    @JsonView(User.UserList.class)
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    public ResponseEntity<ApiResponse> getUsersPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("GET /api/users/page called with page={}, size={}", page, size);
        Page<User> entities = userService.findAll(PageRequest.of(page, size));
        return ResponseEntity.ok(new ApiResponse(new JsonViewPageUtil<>(entities, entities.getPageable())));
    }

    @GetMapping("/{id}")
    @JsonView(User.UserDetail.class)
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR') or hasRole('USER')")
    public ResponseEntity<ApiResponse> getUserById(
            @PathVariable UUID id
    ) {
        log.info("GET /api/users/{} called", id);
        return userService.findById(id)
                .map(entity -> ResponseEntity.ok(new ApiResponse(entity)))
                .orElseGet(() -> {
                    log.error("User with id {} not found", id);
                    throw new RecordNotFoundException();
                });
    }

    @GetMapping("/by-username/{username}")
    @JsonView(User.UserDetail.class)
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR') or hasRole('USER')")
    public ResponseEntity<ApiResponse> getByUsername(
            @PathVariable String username
    ) {
        log.info("GET /api/users/by-username/{} called", username);
        return userService.findByUsername(username)
                .map(entity -> ResponseEntity.ok(new ApiResponse(entity)))
                .orElseGet(() -> {
                    log.error("User with username {} not found", username);
                    throw new RecordNotFoundException();
                });
    }

    @PostMapping
    @JsonView(User.UserDetail.class)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> createUser(
            @RequestBody UserCreateRequest request
    ) {
        log.info("POST /api/users called with payload: {}", request);
        User entity = userService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(entity));
    }

    @PutMapping("/{id}")
    @JsonView(User.UserDetail.class)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateUser(
            @PathVariable UUID id,
            @RequestBody UserUpdateRequest request
    ) {
        log.info("PUT /api/users/{} called with payload: {}", id, request);
        User entity = userService.update(id, request);
        return ResponseEntity.ok(new ApiResponse(entity));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID id
    ) {
        log.info("DELETE /api/users/{} called", id);
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> resetPassword(
            @PathVariable UUID id,
            @RequestBody ResetPasswordRequest request
    ) {
        log.info("POST /api/users/{}/reset-password called", id);
        userService.resetPassword(id, request.getNewPassword());
        return ResponseEntity.noContent().build();
    }
}
