package com.quetoquenana.userservice.repository;

import com.quetoquenana.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    //TODO manage status methods
    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    List<User> findByPersonId(UUID personId);

    // find by external provider and external id (case-insensitive)
    Optional<User> findByExternalProviderIgnoreCaseAndExternalIdIgnoreCase(String externalProvider, String externalId);
}
