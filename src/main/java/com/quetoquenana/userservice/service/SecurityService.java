package com.quetoquenana.userservice.service;

import org.springframework.security.core.Authentication;

/**
 * Authorization helper used from method-security SpEL (e.g. @PreAuthorize("@securityService.canAccessIdNumber(authentication, #idNumber)"))
 */
public interface SecurityService {

    /**
     * Returns true if the authenticated principal is allowed to access data for the given idNumber.
     * - ADMIN role are allowed to access any idNumber.
     * - USER role is allowed only when the idNumber matches the principal identity.
     *
     * @param authentication current authentication
     * @param idNumber the idNumber being requested
     * @return true when access should be allowed
     */
    boolean canAccessIdNumber(Authentication authentication, String idNumber);

    boolean canAccessId(Authentication authentication, String id);
}

