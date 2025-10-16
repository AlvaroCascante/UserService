package com.quetoquenana.userservice.repository;

import com.quetoquenana.userservice.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    Optional<Profile> findByPersonId(UUID personId);
}
