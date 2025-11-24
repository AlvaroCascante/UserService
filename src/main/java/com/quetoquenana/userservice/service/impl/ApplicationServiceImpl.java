package com.quetoquenana.userservice.service.impl;

import com.quetoquenana.userservice.dto.AppRoleCreateRequest;
import com.quetoquenana.userservice.dto.AppRoleUserCreateRequest;
import com.quetoquenana.userservice.dto.ApplicationCreateRequest;
import com.quetoquenana.userservice.dto.ApplicationUpdateRequest;
import com.quetoquenana.userservice.exception.DuplicateRecordException;
import com.quetoquenana.userservice.exception.RecordNotFoundException;
import com.quetoquenana.userservice.model.*;
import com.quetoquenana.userservice.repository.*;
import com.quetoquenana.userservice.service.ApplicationService;
import com.quetoquenana.userservice.service.CurrentUserService;
import com.quetoquenana.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final AppRoleRepository appRoleRepository;
    private final AppRoleUserRepository appRoleUserRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final CurrentUserService currentUserService;
    private final DefaultDataRepository defaultDataRepository;

    @Override
    public Page<Application> findAll(Pageable pageable) {
        return applicationRepository.findAll(pageable);
    }

    @Override
    public Optional<Application> findById(UUID id) {
        return applicationRepository.findById(id);
    }

    @Override
    public Page<Application> searchByName(String name, Pageable pageable) {
        return applicationRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Override
    @Transactional
    public Application save(ApplicationCreateRequest request) {
        if (applicationRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateRecordException("application.name.duplicate");
        }
        Application application = Application.fromCreateRequest(request);
        LocalDateTime now = LocalDateTime.now();
        User currentUser = userRepository.findByUsernameIgnoreCase(currentUserService.getCurrentUsername())
                .orElseThrow(RecordNotFoundException::new);
        application.setCreatedAt(now);
        application.setCreatedBy(currentUser.getUsername());

        // sets role.application and adds to set
        for (DefaultData data : defaultDataRepository.findByDataCategoryAndIsActive(DataCategory.ROLE, true)) {
            AppRole role = AppRole.fromData(data.getDataName(), data.getDescription());
            role.setCreatedAt(now);
            role.setCreatedBy(currentUser.getUsername());
            application.addRole(role);
        }
        return applicationRepository.save(application);
    }

    @Override
    @Transactional
    public Application update(UUID id, ApplicationUpdateRequest request) {
        Application existing = applicationRepository.findById(id)
                .orElseThrow(RecordNotFoundException::new);

        existing.updateFromRequest(request, currentUserService.getCurrentUsername());
        return applicationRepository.save(existing);
    }

    @Override
    @Transactional
    public AppRole addRole(UUID applicationId, AppRoleCreateRequest request) {
        Application existing = applicationRepository.findById(applicationId)
                .orElseThrow(RecordNotFoundException::new);
        AppRole appRole = AppRole.fromCreateRequest(existing, request);
        return saveAppRole(appRole);
    }

    private AppRole saveAppRole(AppRole role) {
        Application application = role.getApplication();

        // If role already exists for the application, return it (avoid merging detached instances)
        Optional<AppRole> existing = appRoleRepository.findByApplicationIdAndRoleName(application.getId(), role.getRoleName());
        if (existing.isPresent()) {
            return existing.get();
        }

        // set auditing for new role
        role.setCreatedAt(LocalDateTime.now());
        role.setCreatedBy(currentUserService.getCurrentUsername());

        // Ensure the application reference is set and application already exists
        if (application.getId() == null) {
            // persist application first if not yet persisted
            application = applicationRepository.saveAndFlush(application);
            role.setApplication(application);
        }

        // Persist role directly to ensure id/version are initialized
        return appRoleRepository.saveAndFlush(role);
    }
    @Override
    @Transactional
    public AppRoleUser addUser(UUID applicationId, AppRoleUserCreateRequest request) {
        // validate application exists
        applicationRepository.findById(applicationId)
                .orElseThrow(RecordNotFoundException::new);

        // find matching app role for the application
        AppRole role = appRoleRepository.findByApplicationIdAndRoleName(applicationId, request.getRoleName())
                .orElseThrow(RecordNotFoundException::new);

        // Step 1: see if a User with the username already exists. If yes, reuse it.
        User user = userService.findByUsername(request.getUser().getUsername())
                .orElseGet(() -> userService.save(request.getUser()));

        // check if mapping already exists for this user and application
        List<AppRoleUser> existingMappings = appRoleUserRepository.findByUserIdAndRoleApplicationId(user.getId(), applicationId);
        if (!existingMappings.isEmpty()) {
            throw new DuplicateRecordException("application.role.user.duplicate");
        }

        // create match record
        AppRoleUser appRoleUser = AppRoleUser.of(user, role);
        appRoleUser.setCreatedAt(LocalDateTime.now());
        appRoleUser.setCreatedBy(currentUserService.getCurrentUsername());
        return appRoleUserRepository.save(appRoleUser);
    }

    @Override
    @Transactional
    public void removeUser(UUID applicationId, String username) {
        // validate application exists
        applicationRepository.findById(applicationId)
                .orElseThrow(RecordNotFoundException::new);

        // delete all mappings for this user within the application
        appRoleUserRepository.deleteByRoleApplicationIdAndUserUsernameIgnoreCase(applicationId, username);
    }

    @Override
    @Transactional
    public void deleteRole(UUID applicationId, UUID roleId) {
        // validate application exists
        applicationRepository.findById(applicationId)
                .orElseThrow(RecordNotFoundException::new);

        // load role and verify belongs to the application
        AppRole role = appRoleRepository.findById(roleId)
                .orElseThrow(RecordNotFoundException::new);
        if (role.getApplication() == null || !role.getApplication().getId().equals(applicationId)) {
            throw new RecordNotFoundException();
        }

        // delete mappings for the role and then the role
        appRoleUserRepository.deleteByRoleId(roleId);
        appRoleRepository.delete(role);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        Application existing = applicationRepository.findById(id)
                .orElseThrow(RecordNotFoundException::new);
        if (existing.getIsActive()) {
            existing.setIsActive(false);
            existing.setUpdatedAt(LocalDateTime.now());
            existing.setUpdatedBy(currentUserService.getCurrentUsername());
            applicationRepository.save(existing);
        }
    }
}
