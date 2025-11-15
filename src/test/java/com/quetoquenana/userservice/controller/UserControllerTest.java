package com.quetoquenana.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.quetoquenana.userservice.dto.ResetPasswordRequest;
import com.quetoquenana.userservice.dto.UserUpdateRequest;
import com.quetoquenana.userservice.exception.RecordNotFoundException;
import com.quetoquenana.userservice.model.ApiResponse;
import com.quetoquenana.userservice.model.User;
import com.quetoquenana.userservice.model.UserStatus;
import com.quetoquenana.userservice.service.UserService;
import com.quetoquenana.userservice.util.TestEntityFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UserControllerTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User user;
    private UUID userId;
    private ObjectMapper objectMapper;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .username("Joe")
                .nickname("Johnny")
                .userStatus(UserStatus.ACTIVE)
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    void testGetUserById_NotFound() {
        when(userService.findById(userId)).thenReturn(Optional.empty());
        assertThrows(RecordNotFoundException.class, () -> userController.getUserById(userId));
    }

    @Test
    void testGetUserByUsername_NotFound() {
        String username = "unknown";
        when(userService.findByUsername(username)).thenReturn(Optional.empty());
        assertThrows(RecordNotFoundException.class, () -> userController.getByUsername(username));
    }

    @Test
    void testGetUserById_Found() throws Exception {
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        ResponseEntity<ApiResponse> response = userController.getUserById(userId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        User data = (User) apiResponse.getData();
        assertEquals(user, data);
        String json = objectMapper.writerWithView(User.UserDetail.class).writeValueAsString(data);
        assertTrue(json.contains("id"));
        assertTrue(json.contains("username"));
        assertTrue(json.contains("nickname"));
        assertTrue(json.contains("userStatus"));
    }

    @Test
    void testGetUsersPage_ReturnsPage() throws Exception {
        Page<User> page = new PageImpl<>(Collections.singletonList(user), PageRequest.of(0, 10), 1);
        when(userService.findAll(any())).thenReturn(page);
        ResponseEntity<ApiResponse> response = userController.getUsersPage(0, 10);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        Page<?> data = (Page<?>) apiResponse.getData();
        assertEquals(1, data.getTotalElements());
        String json = objectMapper.writerWithView(User.UserList.class).writeValueAsString(((Page<?>) apiResponse.getData()).getContent());
        assertTrue(json.contains("id"));
        assertTrue(json.contains("username"));
        assertTrue(json.contains("nickname"));
    }

    @Test
    void testUpdateUser_ReturnsUpdated() {
        UserUpdateRequest updateRequest = TestEntityFactory.getUserUpdateRequest();
        when(userService.update(userId, updateRequest)).thenReturn(user);
        ResponseEntity<ApiResponse> response = userController.updateUser(userId, updateRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        User data = (User) apiResponse.getData();
        assertEquals(user, data);
    }

    @Test
    void testDeleteUser_Success() {
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        ResponseEntity<Void> response = userController.deleteUser(userId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testDeleteUser_NotFound() {
        when(userService.findById(userId)).thenReturn(Optional.empty());
        ResponseEntity<Void> response = userController.deleteUser(userId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testResetPassword_Success() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword("newpass");
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        ResponseEntity<Void> response = userController.resetPassword(userId, request);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testGetUserByUsername_Found() throws Exception {
        String username = user.getUsername();
        when(userService.findByUsername(username)).thenReturn(Optional.of(user));
        ResponseEntity<ApiResponse> response = userController.getByUsername(username);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        User data = (User) apiResponse.getData();
        assertEquals(user, data);
        String json = objectMapper.writerWithView(User.UserDetail.class).writeValueAsString(data);
        assertTrue(json.contains("username"));
        assertTrue(json.contains("nickname"));
    }
}
