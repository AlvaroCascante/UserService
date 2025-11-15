package com.quetoquenana.userservice.repository;

import com.quetoquenana.userservice.model.DefaultData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DefaultDataRepository extends JpaRepository<DefaultData, UUID> {
    Optional<DefaultData> findByDataNameIgnoreCase(String dataName);
    List<DefaultData> findByDataCategoryAndIsActive(String dataCategory, Boolean isActive);
    boolean existsByDataNameIgnoreCase(String dataName);
}

