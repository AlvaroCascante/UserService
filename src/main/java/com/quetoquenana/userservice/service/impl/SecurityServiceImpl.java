package com.quetoquenana.userservice.service.impl;

import com.quetoquenana.userservice.model.AppRoleUser;
import com.quetoquenana.userservice.model.Application;
import com.quetoquenana.userservice.model.User;
import com.quetoquenana.userservice.repository.AppRoleUserRepository;
import com.quetoquenana.userservice.repository.ApplicationRepository;
import com.quetoquenana.userservice.repository.UserRepository;
import com.quetoquenana.userservice.service.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link SecurityService} used from method-security SpEL (bean name: "securityService").
 */
@Service("securityService")
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService {

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final AppRoleUserRepository appRoleUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(String username, String password, String applicationName) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // find application
        Application application = applicationRepository.findByName(applicationName)
                .orElseThrow(() -> new BadCredentialsException("Unknown application: " + applicationName));

        // get roles for user in the application
        List<AppRoleUser> roleMappings = appRoleUserRepository.findByUser_IdAndRole_Application_Id(user.getId(), application.getId());
        List<GrantedAuthority> authorities = roleMappings.stream()
                .map(mapping -> new SimpleGrantedAuthority(mapping.getRole().getRolName()))
                .collect(Collectors.toList());

        // create an authenticated token with authorities
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

    @Override
    public Authentication getAuthenticationForApplication(String username, String applicationName) {
        // find user existence
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid username"));

        Application application = applicationRepository.findByName(applicationName)
                .orElseThrow(() -> new BadCredentialsException("Unknown application: " + applicationName));

        List<AppRoleUser> roleMappings = appRoleUserRepository.findByUser_IdAndRole_Application_Id(user.getId(), application.getId());
        List<GrantedAuthority> authorities = roleMappings.stream()
                .map(mapping -> new SimpleGrantedAuthority(mapping.getRole().getRolName()))
                .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

    @Override
    public boolean canAccessIdNumber(Authentication authentication, String idNumber) {
        if (authentication == null || authentication.getName() == null) return false;
        return userRepository.findByUsernameIgnoreCase(authentication.getName())
                .map(u -> u.getPerson() != null && idNumber != null && idNumber.equals(u.getPerson().getIdNumber()))
                .orElse(false);
    }

    @Override
    public boolean canAccessId(Authentication authentication, String id) {
        if (authentication == null || authentication.getName() == null) return false;
        return userRepository.findByUsernameIgnoreCase(authentication.getName())
                .map(u -> u.getPerson() != null && id != null && id.equals(u.getPerson().getId().toString()))
                .orElse(false);
    }
}
