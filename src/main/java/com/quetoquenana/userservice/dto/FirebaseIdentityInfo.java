package com.quetoquenana.userservice.dto;

public record FirebaseIdentityInfo(
        String uid,
        String email,
        boolean emailVerified,
        String name,
        String pictureUrl,
        String signInProvider
) {}