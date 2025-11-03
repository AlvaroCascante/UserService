package com.quetoquenana.userservice.service;

import org.springframework.security.core.Authentication;

/**
 * Authorization helper used from method-security SpEL (e.g. @PreAuthorize("@securityService.canAccessIdNumber(authentication, #idNumber)"))
 */
public interface SecurityService {

    boolean canAccessIdNumber(Authentication authentication, String idNumber);

    boolean canAccessId(Authentication authentication, String id);
}

