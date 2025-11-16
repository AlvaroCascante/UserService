package com.quetoquenana.userservice.service;

import com.quetoquenana.userservice.dto.DefaultDataCreateRequest;
import com.quetoquenana.userservice.dto.DefaultDataUpdateRequest;
import com.quetoquenana.userservice.model.DefaultData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DefaultDataService {
    Page<DefaultData> findAll(Pageable pageable);

    Optional<DefaultData> findById(UUID id);

    Page<DefaultData> findByDataCategory(String category, Pageable pageable);

    DefaultData create(DefaultDataCreateRequest request);

    DefaultData update(UUID id, DefaultDataUpdateRequest request);

    void deleteById(UUID id);
}

