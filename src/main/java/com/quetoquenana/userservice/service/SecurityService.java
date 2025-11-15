package com.quetoquenana.userservice.service;

import org.springframework.security.core.Authentication;

import java.util.UUID;

/**
 * Authorization helper used from method-security SpEL (e.g. @PreAuthorize("@securityService.canAccessIdNumber(authentication, #idNumber)"))
 */
public interface SecurityService {

    Authentication authenticate(String username, String password, String applicationName);
    Authentication getAuthenticationForApplication(String username, String applicationName);
    void resetPassword(String username, String newPassword);

    boolean canAccessIdNumber(Authentication authentication, String idNumber);
    boolean canAccessIdProfile(Authentication authentication, UUID idProfile);
    boolean canAccessIdPerson(Authentication authentication, UUID idPerson);
    boolean canAccessIdAddress(Authentication authentication, UUID idAddress);
    boolean canAccessIdPhone(Authentication authentication, UUID idPhone);
    boolean canAccessIdUser(Authentication authentication, UUID idUser);
}
