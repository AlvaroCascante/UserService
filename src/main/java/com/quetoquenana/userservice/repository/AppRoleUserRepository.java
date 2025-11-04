package com.quetoquenana.userservice.repository;

import com.quetoquenana.userservice.model.AppRoleUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppRoleUserRepository extends JpaRepository<AppRoleUser, UUID> {
}

