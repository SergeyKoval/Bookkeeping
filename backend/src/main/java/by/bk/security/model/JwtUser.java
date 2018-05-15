package by.bk.security.model;

import by.bk.entity.user.UserPermission;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * @author Sergey Koval
 */
public class JwtUser implements UserDetails {
    private String username;
    private String password;
    private List<UserPermission> authorities;

    public JwtUser() {
    }

    public JwtUser(String username, String password, List<UserPermission> permissions) {
        this.username = username;
        this.authorities = permissions;
        this.password = password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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
        return true;
    }
}