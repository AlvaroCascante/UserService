package com.quetoquenana.personservice.service;

import com.quetoquenana.personservice.model.Execution;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExecutionService {
    List<Execution> findAll();
    Optional<Execution> findById(UUID id);
    void saveExecutionOnStartup();
    Page<Execution> findAll(Pageable pageable);
}
