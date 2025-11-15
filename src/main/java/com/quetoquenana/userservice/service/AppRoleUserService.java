package com.quetoquenana.userservice.service;

import com.quetoquenana.userservice.repository.AppRoleUserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppRoleUserService {

    private static final Logger log = LoggerFactory.getLogger(AppRoleUserService.class);

    private final AppRoleUserRepository appRoleUserRepository;

    @Transactional
    public void deleteByUserUsername(String username) {
        log.info("Deleting AppRoleUser mappings for username={}", username);
        appRoleUserRepository.deleteByUserUsernameIgnoreCase(username);
    }

    @Transactional
    public void deleteByApplicationIdAndUsername(UUID applicationId, String username) {
        log.info("Deleting AppRoleUser mappings for applicationId={}, username={}", applicationId, username);
        appRoleUserRepository.deleteByRoleApplicationIdAndUserUsernameIgnoreCase(applicationId, username);
    }

    @Transactional
    public void deleteByApplicationIdUsernameAndRole(UUID applicationId, String username, String roleName) {
        log.info("Deleting AppRoleUser mappings for applicationId={}, username={}, roleName={}", applicationId, username, roleName);
        appRoleUserRepository.deleteByRoleApplicationIdAndUserUsernameIgnoreCaseAndRoleRoleNameIgnoreCase(applicationId, username, roleName);
    }
}

