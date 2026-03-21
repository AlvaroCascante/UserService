package com.quetoquenana.userservice.service;

import com.quetoquenana.userservice.dto.UserCreateFromFirebaseRequest;
import com.quetoquenana.userservice.dto.UserCreateFromFirebaseResponse;

public interface AuthUserService {
    UserCreateFromFirebaseResponse createFromFirebase(UserCreateFromFirebaseRequest request, String appCode);
}

