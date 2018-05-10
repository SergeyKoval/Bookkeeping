package by.bk.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Sergey Koval
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Autowired
    private JwtTokenUtil tokenUtil;
    @Autowired
    private AuthenticationAPI authenticationAPI;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        final SecurityContext securityContext = SecurityContextHolder.getContext();
        final String requestHeader = request.getHeader(TOKEN_HEADER);

        if (StringUtils.startsWith(requestHeader, TOKEN_PREFIX)) {
            final String authToken = StringUtils.substringAfter(requestHeader, TOKEN_PREFIX);
            if (securityContext.getAuthentication() == null && !tokenUtil.isTokenExpired(authToken)) {
                Authentication authentication = authenticationAPI.getAuthentication(tokenUtil.getUsernameFromToken(authToken));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        chain.doFilter(request, response);
    }
}