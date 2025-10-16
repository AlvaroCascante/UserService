package com.quetoquenana.personservice.repository;

import com.quetoquenana.personservice.model.Execution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExecutionRepository extends JpaRepository<Execution, UUID> {
}
