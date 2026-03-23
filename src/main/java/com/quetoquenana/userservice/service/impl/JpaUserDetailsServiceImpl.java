package com.quetoquenana.userservice.service.impl;

import com.quetoquenana.userservice.dto.SecurityUser;
import com.quetoquenana.userservice.exception.AuthenticationException;
import com.quetoquenana.userservice.model.AppRoleUser;
import com.quetoquenana.userservice.model.Application;
import com.quetoquenana.userservice.model.User;
import com.quetoquenana.userservice.repository.AppRoleUserRepository;
import com.quetoquenana.userservice.repository.ApplicationRepository;
import com.quetoquenana.userservice.repository.UserRepository;
import com.quetoquenana.userservice.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Primary
public class JpaUserDetailsServiceImpl implements CustomUserDetailsService {

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final AppRoleUserRepository appRoleUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new AuthenticationException("error.authentication"));

        return new SecurityUser(user, null);
    }

    @Override
    public UserDetails loadUserByUsername(String username, String appCode) throws UsernameNotFoundException {
        SecurityUser securityUser = (SecurityUser)loadUserByUsername(username);
        User user = securityUser.user();

        Application application = applicationRepository.findByCode(appCode)
                .orElseThrow(() -> new AuthenticationException("error.authentication.application"));

        if (!application.isActive()) {
            throw new AuthenticationException("error.authentication.application.inactive");
        }

        List<AppRoleUser> appRoleUsers = appRoleUserRepository.findByUserIdAndRoleApplicationId(user.getId(), application.getId());

        List<GrantedAuthority> authorities = appRoleUsers.stream()
                .map(mapping -> new SimpleGrantedAuthority(mapping.getRole().getRoleName()))
                .collect(Collectors.toList());


        return new SecurityUser(user, authorities);
    }
}
