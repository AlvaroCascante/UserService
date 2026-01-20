package com.quetoquenana.userservice.repository;

import com.quetoquenana.userservice.model.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    //TODO manage status methods
    Optional<Application> findByName(String name);

    List<Application> findByActiveTrue();

    boolean existsByNameIgnoreCase(String name);

    Page<Application> findByNameContainingIgnoreCase(String name, Pageable pageable);
}

