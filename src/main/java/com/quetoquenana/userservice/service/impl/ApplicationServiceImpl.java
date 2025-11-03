package com.quetoquenana.userservice.service.impl;

import com.quetoquenana.userservice.dto.ApplicationCreateRequest;
import com.quetoquenana.userservice.dto.ApplicationUpdateRequest;
import com.quetoquenana.userservice.exception.DuplicateRecordException;
import com.quetoquenana.userservice.exception.RecordNotFoundException;
import com.quetoquenana.userservice.model.Application;
import com.quetoquenana.userservice.repository.ApplicationRepository;
import com.quetoquenana.userservice.service.ApplicationService;
import com.quetoquenana.userservice.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CurrentUserService currentUserService;

    @Override
    public List<Application> findAll() {
        return applicationRepository.findAll();
    }

    @Override
    public Page<Application> findAll(Pageable pageable) {
        return applicationRepository.findAll(pageable);
    }

    @Override
    public Optional<Application> findById(UUID id) {
        return applicationRepository.findById(id);
    }

    @Override
    public Optional<Application> findByName(String name) {
        return applicationRepository.findByName(name);
    }

    @Override
    public Page<Application> searchByName(String name, Pageable pageable) {
        return applicationRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Override
    @Transactional
    public Application save(ApplicationCreateRequest request) {
        if (applicationRepository.existsByName(request.getName())) {
            throw new DuplicateRecordException("application.name.duplicate");
        }

        Application application = Application.fromCreateRequest(request);
        application.setCreatedAt(LocalDateTime.now());
        application.setCreatedBy(currentUserService.getCurrentUsername());
        return applicationRepository.save(application);
    }

    @Override
    @Transactional
    public Application update(UUID id, ApplicationUpdateRequest request) {
        Application existing = applicationRepository.findById(id)
                .orElseThrow(RecordNotFoundException::new);

        existing.updateFromRequest(request, currentUserService.getCurrentUsername());
        return applicationRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        Application existing = applicationRepository.findById(id)
                .orElseThrow(RecordNotFoundException::new);
        if (existing.isActive()) {
            existing.setActive(false);
            existing.setUpdatedAt(LocalDateTime.now());
            existing.setUpdatedBy(currentUserService.getCurrentUsername());
            applicationRepository.save(existing);
        }
    }
}
