package com.quetoquenana.userservice.service.impl;

import com.quetoquenana.userservice.dto.DefaultDataCreateRequest;
import com.quetoquenana.userservice.dto.DefaultDataUpdateRequest;
import com.quetoquenana.userservice.exception.DuplicateRecordException;
import com.quetoquenana.userservice.exception.RecordNotFoundException;
import com.quetoquenana.userservice.model.DataCategory;
import com.quetoquenana.userservice.model.DefaultData;
import com.quetoquenana.userservice.repository.DefaultDataRepository;
import com.quetoquenana.userservice.service.CurrentUserService;
import com.quetoquenana.userservice.service.DefaultDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DefaultDataServiceImpl implements DefaultDataService {

    private final DefaultDataRepository defaultDataRepository;
    private final CurrentUserService currentUserService;

    @Override
    public Page<DefaultData> findAll(Pageable pageable) {
        return defaultDataRepository.findAll(pageable);
    }

    @Override
    public Optional<DefaultData> findById(UUID id) {
        return defaultDataRepository.findById(id);
    }

    @Override
    public Page<DefaultData> findByDataCategory(String category, Pageable pageable) {
        return defaultDataRepository.findByDataCategory(DataCategory.valueOf(category), pageable);
    }

    @Override
    @Transactional
    public DefaultData create(DefaultDataCreateRequest request) {
        if (defaultDataRepository.existsByDataNameIgnoreCase(request.getName())) {
            throw new DuplicateRecordException("defaultData.name.duplicate");
        }
        DefaultData entity = DefaultData.fromCreate(request);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(currentUserService.getCurrentUsername());
        return defaultDataRepository.save(entity);
    }

    @Override
    @Transactional
    public DefaultData update(UUID id, DefaultDataUpdateRequest request) {
        DefaultData existing = defaultDataRepository.findById(id)
                .orElseThrow(RecordNotFoundException::new);
        existing.updateFromRequest(request);
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(currentUserService.getCurrentUsername());
        return defaultDataRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        DefaultData existing = defaultDataRepository.findById(id)
                .orElseThrow(RecordNotFoundException::new);
        if (existing.getIsActive()) {
            existing.setIsActive(false);
            existing.setUpdatedAt(LocalDateTime.now());
            existing.setUpdatedBy(currentUserService.getCurrentUsername());
            defaultDataRepository.save(existing);
        }
    }
}

