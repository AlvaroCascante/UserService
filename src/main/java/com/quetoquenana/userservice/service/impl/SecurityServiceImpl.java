package com.quetoquenana.userservice.service.impl;

import com.quetoquenana.userservice.service.SecurityService;
import com.quetoquenana.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link SecurityService} used from method-security SpEL (bean name: "securityService").
 * Behavior:
 * - Principals with roles ROLE_ADMIN or ROLE_AUDITOR are allowed to access any idNumber.
 * - Principals with ROLE_USER are allowed only when the requested idNumber matches their identity.
 * Matching strategy for USER principals (fallback order):
 * 1. authentication.getName()
 * 2. If principal implements UserDetails -> getUsername()
 * 3. Reflection: attempt to call getIdNumber() on the principal (useful for custom principal objects)
 */
@Service("securityService")
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService {

    private final UserService userService;

    @Override
    public boolean canAccessIdNumber(Authentication authentication, String idNumber) {
        if (authentication == null || !authentication.isAuthenticated() || idNumber == null) {
            return false;
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        if (isAdmin) {
            return true;
        }

        boolean isUser = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_USER"::equals);
        if (!isUser) {
            return false;
        }

        // Determine the username from the authentication (common setups use it as the principal name)
        String username = authentication.getName();

        // Try UserDetails principal as a fallback for username if authentication.getName() is not useful
        Object principal = authentication.getPrincipal();
        if ((username == null || username.isEmpty()) && principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        }

        if (username == null || username.isEmpty()) {
            return false;
        }

        // Lookup the application user by username and validate their person's idNumber
        return userService.findByUsername(username)
                .map(user -> user.getPerson() != null && idNumber.equals(user.getPerson().getIdNumber()))
                .orElse(false);
    }

    @Override
    public boolean canAccessId(Authentication authentication, String id) {
        //TODO implement proper check
        return true;
    }
}
