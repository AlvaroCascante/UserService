package com.quetoquenana.userservice.service;

import com.quetoquenana.userservice.dto.AppRoleCreateRequest;
import com.quetoquenana.userservice.dto.ApplicationCreateRequest;
import com.quetoquenana.userservice.dto.ApplicationUpdateRequest;
import com.quetoquenana.userservice.dto.AppRoleUserCreateRequest;
import com.quetoquenana.userservice.model.AppRole;
import com.quetoquenana.userservice.model.AppRoleUser;
import com.quetoquenana.userservice.model.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ApplicationService {
    Page<Application> findAll(Pageable pageable);

    Optional<Application> findById(UUID id);

    Page<Application> searchByName(String name, Pageable pageable);

    Application save(ApplicationCreateRequest request);

    Application update(UUID id, ApplicationUpdateRequest request);

    AppRole addRole(UUID applicationId, AppRoleCreateRequest request);

    AppRoleUser addUser(UUID applicationId, AppRoleUserCreateRequest request);

    void removeUser(UUID applicationId, String username);

    void deleteRole(UUID applicationId, UUID roleId);

    void deleteById(UUID id);
}
