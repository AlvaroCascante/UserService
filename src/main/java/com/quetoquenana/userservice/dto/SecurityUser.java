package com.quetoquenana.userservice.dto;

import com.quetoquenana.userservice.model.User;
import com.quetoquenana.userservice.model.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public record SecurityUser(User user, Collection<? extends GrantedAuthority> authorities) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.getUserStatus() != UserStatus.INACTIVE && user.getUserStatus() != UserStatus.BLOCKED;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !user.accountLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getUserStatus() != UserStatus.INACTIVE && user.getUserStatus() != UserStatus.BLOCKED;
    }
}
