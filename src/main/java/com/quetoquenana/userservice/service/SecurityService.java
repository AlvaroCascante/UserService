package com.quetoquenana.userservice.service;

import org.springframework.security.core.Authentication;

/**
 * Authorization helper used from method-security SpEL (e.g. @PreAuthorize("@securityService.canAccessIdNumber(authentication, #idNumber)"))
 */
public interface SecurityService {
    /**
     * Authenticate the username/password for the provided application name.
     * Returns an authenticated Authentication (with granted authorities) on success.
     * Throws an exception (e.g. BadCredentialsException) on failure.
     */
    Authentication authenticate(String username, String password, String applicationName);

    boolean canAccessIdNumber(Authentication authentication, String idNumber);

    boolean canAccessId(Authentication authentication, String id);

    /**
     * After a successful credential authentication (e.g. via httpBasic), build an Authentication
     * populated with authorities for the given application name. This does not validate credentials.
     */
    Authentication getAuthenticationForApplication(String username, String applicationName);
}
