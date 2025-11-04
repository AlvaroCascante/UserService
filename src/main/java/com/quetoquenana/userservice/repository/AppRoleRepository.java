package com.quetoquenana.userservice.repository;

import com.quetoquenana.userservice.model.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppRoleRepository extends JpaRepository<AppRole, UUID> {
    List<AppRole> findByApplicationId(UUID applicationId);
}

