package com.quetoquenana.userservice.repository;

import com.quetoquenana.userservice.model.AppRoleUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AppRoleUserRepository extends JpaRepository<AppRoleUser, UUID> {
    List<AppRoleUser> findByUserIdAndRoleApplicationId(UUID userId, UUID applicationId);

    // Delete all AppRoleUser mappings for a given username (case-insensitive)
    void deleteByUserUsernameIgnoreCase(String username);

    // Delete mappings for a given application (role.application.id) and username (case-insensitive)
    void deleteByRoleApplicationIdAndUserUsernameIgnoreCase(UUID applicationId, String username);

    // Delete mappings for a given application, username and role name (all case-insensitive for textual values)
    void deleteByRoleApplicationIdAndUserUsernameIgnoreCaseAndRoleRoleNameIgnoreCase(UUID applicationId, String username, String rolName);

    // Delete mappings for a given role id (used before deleting the AppRole)
    void deleteByRoleId(UUID roleId);
}
