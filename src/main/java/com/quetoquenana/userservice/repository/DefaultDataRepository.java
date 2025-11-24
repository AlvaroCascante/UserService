package com.quetoquenana.userservice.repository;

import com.quetoquenana.userservice.model.DataCategory;
import com.quetoquenana.userservice.model.DefaultData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DefaultDataRepository extends JpaRepository<DefaultData, UUID> {
    Page<DefaultData> findByDataCategory(DataCategory dataCategory, Pageable pageable);
    List<DefaultData> findByDataCategoryAndIsActive(DataCategory dataCategory, Boolean isActive);
    boolean existsByDataNameIgnoreCase(String dataName);
}

