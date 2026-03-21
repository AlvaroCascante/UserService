package com.quetoquenana.userservice.service;

import com.google.firebase.auth.FirebaseToken;

public interface FirebaseTokenVerifier {
    /**
     * Verify the Firebase ID token and return the decoded token.
     * Implementations should throw an exception for invalid/expired tokens.
     */
    FirebaseToken verify(String idToken);
}

