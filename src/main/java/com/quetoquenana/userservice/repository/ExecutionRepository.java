package com.quetoquenana.userservice.repository;

import com.quetoquenana.userservice.model.Execution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExecutionRepository extends JpaRepository<Execution, UUID> {
}
