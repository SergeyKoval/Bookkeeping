package by.bk.security;

import by.bk.security.model.JwtToken;
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
    @Autowired
    private JwtTokenUtil tokenUtil;
    @Autowired
    private AuthenticationAPI authenticationAPI;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        final SecurityContext securityContext = SecurityContextHolder.getContext();
        final String requestHeader = request.getHeader(JwtToken.TOKEN_HEADER);

        if (StringUtils.startsWith(requestHeader, JwtToken.TOKEN_PREFIX)) {
                JwtToken token = JwtToken.from(requestHeader, tokenUtil);
                if (securityContext.getAuthentication() == null && !token.isExpired()) {
                    Authentication authentication = authenticationAPI.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "EXPIRED");
                    return;
                }
        }

        chain.doFilter(request, response);
    }
}
