package com.quetoquenana.userservice.service;

import com.quetoquenana.userservice.dto.UserCreateRequest;
import com.quetoquenana.userservice.dto.UserUpdateRequest;
import com.quetoquenana.userservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    User save(UserCreateRequest request);

    User update(UUID id, UserUpdateRequest request);

    void deleteById(UUID id);

    void resetPassword(UUID id, String newPassword);

    Optional<User> findById(UUID id);

    Page<User> findAll(Pageable pageable);

    Optional<User> findByUsername(String username);
}