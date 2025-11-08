package com.quetoquenana.userservice.repository;

import com.quetoquenana.userservice.model.AppRoleUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AppRoleUserRepository extends JpaRepository<AppRoleUser, UUID> {
    List<AppRoleUser> findByUser_IdAndRole_Application_Id(UUID userId, UUID applicationId);
}
