package com.quetoquenana.userservice.service;

import com.quetoquenana.userservice.dto.UserCreateFromFirebaseRequest;
import com.quetoquenana.userservice.dto.UserCreateFromFirebaseResponse;
import com.quetoquenana.userservice.dto.UserCreateRequest;
import com.quetoquenana.userservice.model.AppRoleUser;

public interface AuthUserService {
    UserCreateFromFirebaseResponse createFromFirebase(UserCreateFromFirebaseRequest request, String appCode);
}

