package by.bk.entity.user;

import by.bk.security.model.JwtUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * @author Sergey Koval
 */
@Service
public class UserService implements UserAPI, UserDetailsService {
    @Override
    public JwtUser loadUserByUsername(String username) throws UsernameNotFoundException {
        return new JwtUser(username, "$2a$10$cMO13d9W3yS2HMcGHrA/reiR8rQW7poKZIlbe6rrlzL493yy4FrtC", Collections.singletonList(UserPermission.USER));
    }

    @Override
    public Authentication getAuthentication(String username) {
        JwtUser jwtUser = new JwtUser(username, null, Collections.singletonList(UserPermission.USER));
        return new UsernamePasswordAuthenticationToken(jwtUser, null, jwtUser.getAuthorities());
    }
}