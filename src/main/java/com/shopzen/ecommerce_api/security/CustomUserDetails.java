// src/main/java/com/shopzen/ecommerce_api/security/CustomUserDetails.java
package com.shopzen.ecommerce_api.security;

import com.shopzen.ecommerce_api.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.user = user;
        
        Boolean isAdmin = user.getIsAdmin();
        if (isAdmin != null && isAdmin) {
            this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }
    }

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
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        Boolean isActive = user.getIsActive();
        return isActive != null && isActive;
    }

    public String getUserId() {
        return user.getId();  // Changed from UUID to String - no toString() needed
    }

    public String getEmail() {
        return user.getEmail();
    }

    public Boolean getIsAdmin() {
        Boolean isAdmin = user.getIsAdmin();
        return isAdmin != null && isAdmin;
    }

    public String getFullName() {
        return user.getFullName();
    }
}