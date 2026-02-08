package com.quetoquenana.userservice.service;

import com.google.firebase.auth.FirebaseToken;
import com.quetoquenana.userservice.model.User;

public interface AuthUserService {
    /**
     * Resolve or create a backend user from a decoded Firebase token.
     * Returns a ResolveResult containing the User and a flag indicating if it was newly created.
     */
    ResolveResult resolveOrCreateFromFirebase(FirebaseToken decoded);

    class ResolveResult {
        private final User user;
        private final boolean newUser;

        public ResolveResult(User user, boolean newUser) {
            this.user = user;
            this.newUser = newUser;
        }

        public User getUser() {
            return user;
        }

        public boolean isNewUser() {
            return newUser;
        }
    }
}

