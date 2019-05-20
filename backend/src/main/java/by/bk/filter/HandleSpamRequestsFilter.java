package by.bk.filter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HandleSpamRequestsFilter extends OncePerRequestFilter {
    private static final String ACCEPT = "Accept";
    private static final String INVALID_MIME_TYPE = "Invalid mime type";

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        String header = httpServletRequest.getHeader(ACCEPT);
        if (StringUtils.isNotBlank(header)) {
            try {
                MediaType.parseMediaTypes(header);
            } catch (InvalidMimeTypeException e) {
                httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, INVALID_MIME_TYPE);
                return;
            }
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}