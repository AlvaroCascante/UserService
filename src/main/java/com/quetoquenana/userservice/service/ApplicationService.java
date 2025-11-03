package com.quetoquenana.userservice.service;

import com.quetoquenana.userservice.dto.ApplicationCreateRequest;
import com.quetoquenana.userservice.dto.ApplicationUpdateRequest;
import com.quetoquenana.userservice.model.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationService {
    List<Application> findAll();

    Page<Application> findAll(Pageable pageable);

    Optional<Application> findById(UUID id);

    Optional<Application> findByName(String name);

    Page<Application> searchByName(String name, Pageable pageable);

    Application save(ApplicationCreateRequest request);

    Application update(UUID id, ApplicationUpdateRequest request);

    void deleteById(UUID id);
}
