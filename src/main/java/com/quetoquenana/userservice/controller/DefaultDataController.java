package com.quetoquenana.userservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.userservice.dto.DefaultDataCreateRequest;
import com.quetoquenana.userservice.dto.DefaultDataUpdateRequest;
import com.quetoquenana.userservice.exception.RecordNotFoundException;
import com.quetoquenana.userservice.model.ApiResponse;
import com.quetoquenana.userservice.model.DefaultData;
import com.quetoquenana.userservice.model.Application;
import com.quetoquenana.userservice.service.DefaultDataService;
import com.quetoquenana.userservice.util.JsonViewPageUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/default-data")
@RequiredArgsConstructor
@Slf4j
public class DefaultDataController {

    private final DefaultDataService defaultDataService;

    @GetMapping("/page")
    @PreAuthorize("hasRole('SYSTEM')")
    @JsonView(DefaultData.DefaultDataList.class)
    public ResponseEntity<ApiResponse> getDefaultDataPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("GET /api/default-data/page called with page={}, size={}", page, size);
        Page<DefaultData> entities = defaultDataService.findAll(PageRequest.of(page, size));
        return ResponseEntity.ok(new ApiResponse(new JsonViewPageUtil<>(entities, entities.getPageable())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM')")
    @JsonView(DefaultData.DefaultDataDetail.class)
    public ResponseEntity<ApiResponse> getDefaultDataById(
            @PathVariable UUID id
    ) {
        log.info("GET /api/default-data/{} called", id);
        return defaultDataService.findById(id)
                .map(entity -> ResponseEntity.ok(new ApiResponse(entity)))
                .orElseGet(() -> {
                    log.error("DefaultData with id {} not found", id);
                    throw new RecordNotFoundException();
                });
    }

    @GetMapping("/page/category/{category}")
    @PreAuthorize("hasRole('SYSTEM')")
    @JsonView(DefaultData.DefaultDataDetail.class)
    public ResponseEntity<ApiResponse> getDefaultDataByCategory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @PathVariable String category
    ) {
        log.info("GET /api/default-data/page/category/{} called", category);
        Page<DefaultData> entities = defaultDataService.findByDataCategory(category, PageRequest.of(page, size));
        return ResponseEntity.ok(new ApiResponse(new JsonViewPageUtil<>(entities, entities.getPageable())));
    }

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM')")
    @JsonView(DefaultData.DefaultDataDetail.class)
    public ResponseEntity<ApiResponse> createDefaultData(
            @Valid @RequestBody DefaultDataCreateRequest request
    ) {
        log.info("POST /api/default-data called with payload: {}", request);
        DefaultData saved = defaultDataService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM')")
    @JsonView(DefaultData.DefaultDataDetail.class)
    public ResponseEntity<ApiResponse> updateDefaultData(@PathVariable UUID id, @RequestBody DefaultDataUpdateRequest request) {
        log.info("PUT /api/default-data/{} called with payload: {}", id, request);
        DefaultData saved = defaultDataService.update(id, request);
        return ResponseEntity.ok(new ApiResponse(saved));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM')")
    public ResponseEntity<Void> deleteDefaultData(@PathVariable UUID id) {
        log.info("DELETE /api/default-data/{} called", id);
        defaultDataService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
