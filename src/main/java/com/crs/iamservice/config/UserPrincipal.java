package com.crs.iamservice.config;

import com.crs.iamservice.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.stream.Collectors;

public record UserPrincipal(
        String id,
        String email,
        String password,
        Collection<? extends GrantedAuthority> authorities
) implements UserDetails {

    public static UserPrincipal create(User user) {
        // Lấy quyền từ action của Permissions
        var authorities = user.getRole().getPermissions().stream()
                .map(p -> new SimpleGrantedAuthority(p.getAction()))
                .collect(Collectors.toList());

        // Thêm quyền từ Role (định dạng ROLE_ADMIN)
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));

        return new UserPrincipal(
                user.getUserId(),
                user.getEmail(),
                user.getPasswordHash(),
                authorities
        );
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}