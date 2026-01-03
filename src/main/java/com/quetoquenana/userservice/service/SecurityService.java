package com.quetoquenana.userservice.service;

import com.quetoquenana.userservice.dto.ChangePasswordRequest;
import com.quetoquenana.userservice.dto.ResetUserRequest;
import org.springframework.security.core.Authentication;

import java.util.UUID;

/**
 * Authorization helper used from method-security SpEL (e.g. @PreAuthorize("@securityService.canAccessIdNumber(authentication, #idNumber)"))
 */
public interface SecurityService {

    void login(Authentication authentication);
    void resetUser(Authentication authentication, ChangePasswordRequest request);
    void forgotPassword(String username);
    boolean canAccessIdNumber(Authentication authentication, String idNumber);
    boolean canAccessIdProfile(Authentication authentication, UUID idProfile);
    boolean canAccessIdPerson(Authentication authentication, UUID idPerson);
    boolean canAccessIdAddress(Authentication authentication, UUID idAddress);
    boolean canAccessIdPhone(Authentication authentication, UUID idPhone);
    boolean canAccessIdUser(Authentication authentication, UUID idUser);
}
