package com.quetoquenana.personservice.service.impl;

import com.quetoquenana.personservice.model.Execution;
import com.quetoquenana.personservice.repository.ExecutionRepository;
import com.quetoquenana.personservice.service.ExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExecutionServiceImpl implements ExecutionService {
    private final ExecutionRepository executionRepository;

    @Value("${app.version:0.0.1}")
    private String appVersion;

    @Value("${spring.profiles.active:dev}")
    private String environment;

    /**
     * Save a new execution record. Called by StartupExecutionRecorder.
     */
    public void saveExecutionOnStartup() {
        Execution execution = new Execution();
        execution.setId(UUID.randomUUID());
        execution.setExecutedAt(LocalDateTime.now());
        try {
            execution.setServerName(InetAddress.getLocalHost().getHostName());
            execution.setIpAddress(InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            execution.setServerName("unknown");
            execution.setIpAddress("unknown");
        }
        execution.setAppVersion(appVersion);
        execution.setEnvironment(environment);
        executionRepository.save(execution);
    }

    @Override
    public List<Execution> findAll() {
        return executionRepository.findAll();
    }

    @Override
    public Optional<Execution> findById(UUID id) {
        return executionRepository.findById(id);
    }

    @Override
    public Page<Execution> findAll(Pageable pageable) {
        return executionRepository.findAll(pageable);
    }
}
