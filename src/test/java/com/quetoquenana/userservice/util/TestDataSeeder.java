package com.quetoquenana.userservice.util;

import com.quetoquenana.userservice.model.*;
import com.quetoquenana.userservice.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

public class TestDataSeeder {

    /**
     * Ensure an Application exists, an AppRole exists for that application, create a User linked to the provided Person,
     * and map the user to the role. Idempotent: will delete existing user/mappings for the username before creating.
     */
    public static User seedUserWithRole(
            ApplicationRepository applicationRepository,
            AppRoleRepository appRoleRepository,
            UserRepository userRepository,
            AppRoleUserRepository appRoleUserRepository,
            PasswordEncoder passwordEncoder,
            Person person,
            String applicationName,
            String roleName,
            String username,
            String rawPassword
    ) {
        // Ensure application
        Application application = applicationRepository.findByName(applicationName)
                .orElseGet(() -> {
                    Application app = Application.builder()
                            .name(applicationName)
                            .description("Test application")
                            .isActive(true)
                            .build();
                    app.setCreatedAt(LocalDateTime.now());
                    app.setCreatedBy(username);
                    return applicationRepository.save(app);
                });

        // Ensure role exists for application
        AppRole role = appRoleRepository.findAll().stream()
                .filter(r -> r.getApplication() != null && r.getApplication().getId() != null
                        && r.getApplication().getId().equals(application.getId())
                        && roleName.equalsIgnoreCase(r.getRoleName()))
                .findFirst()
                .orElseGet(() -> {
                    AppRole r = AppRole.fromData(roleName, "Role for tests");
                    r.setCreatedAt(LocalDateTime.now());
                    r.setCreatedBy(username);
                    r.setApplication(application);
                    return appRoleRepository.save(r);
                });

        // Clean previous user/mappings for this username to keep idempotent
        try {
            appRoleUserRepository.deleteByUserUsernameIgnoreCase(username);
        } catch (Exception ignored) {
            // ignore any errors from delete when no previous records
        }
        userRepository.findByUsernameIgnoreCase(username).ifPresent(userRepository::delete);

        // Create user linked to provided person
        User user = User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .person(person)
                .nickname("test-nick")
                .userStatus(UserStatus.ACTIVE)
                .build();
        user.setCreatedAt(LocalDateTime.now());
        user.setCreatedBy(username);
        user = userRepository.save(user);

        AppRoleUser aru = AppRoleUser.of(user, role);
        aru.setCreatedAt(LocalDateTime.now());
        aru.setCreatedBy(username);
        appRoleUserRepository.save(aru);

        return user;

    }
}
