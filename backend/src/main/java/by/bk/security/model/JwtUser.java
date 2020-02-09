package by.bk.security.model;

import by.bk.entity.user.UserPermission;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

/**
 * @author Sergey Koval
 */
@ToString
public class JwtUser implements UserDetails {
    private String username;
    private String password;
    private List<UserPermission> authorities;
    private boolean enabled;
    private String deviceId;

    public JwtUser() {
    }

    public JwtUser(String username, String password, boolean enabled, List<UserPermission> permissions, String deviceId) {
        this.username = username;
        this.authorities = permissions;
        this.password = password;
        this.enabled = enabled;
        this.deviceId = deviceId;
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
        return enabled;
    }

    public String getDeviceId() {
        return deviceId;
    }
}
