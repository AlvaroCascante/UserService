package com.quetoquenana.userservice.service;

import com.quetoquenana.userservice.dto.UserCreateRequest;
import com.quetoquenana.userservice.dto.UserUpdateRequest;
import com.quetoquenana.userservice.model.AppRoleUser;
import com.quetoquenana.userservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    List<User> findAll();

    List<AppRoleUser> findAllAppRoleByApplicationId(UUID idUser, UUID idApplication);

    Page<User> findAll(Pageable pageable);

    Optional<User> findById(UUID id);

    Optional<User> findByUsername(String username);

    User save(UserCreateRequest request);

    User update(UUID id, UserUpdateRequest request);

    void delete(UUID id);

    void resetPassword(UUID id, String newPassword);
}