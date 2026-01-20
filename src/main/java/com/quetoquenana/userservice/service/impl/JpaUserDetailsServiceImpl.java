package com.quetoquenana.userservice.service.impl;

import com.quetoquenana.userservice.dto.SecurityUser;
import com.quetoquenana.userservice.exception.AuthenticationException;
import com.quetoquenana.userservice.model.AppRoleUser;
import com.quetoquenana.userservice.model.Application;
import com.quetoquenana.userservice.model.User;
import com.quetoquenana.userservice.repository.AppRoleUserRepository;
import com.quetoquenana.userservice.repository.ApplicationRepository;
import com.quetoquenana.userservice.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.stream.Collectors;

import static com.quetoquenana.userservice.util.Constants.Headers.APP_NAME;

@Service
@RequiredArgsConstructor
@Primary
public class JpaUserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final AppRoleUserRepository appRoleUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new AuthenticationException("error.authentication"));

        Application application = applicationRepository.findByName(getAppNameFromRequest())
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

    private String getAppNameFromRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest req = attrs.getRequest();
            return req.getHeader(APP_NAME);
        }
        throw new AuthenticationException("error.authentication.application.header");
    }
}
