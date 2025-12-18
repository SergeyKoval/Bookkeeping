package by.bk.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to reject spam and malformed requests early in the request pipeline.
 * This filter runs at highest precedence to block invalid requests before
 * they consume server resources.
 *
 * @author Sergey Koval
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HandleSpamRequestsFilter extends OncePerRequestFilter {
    private static final String ACCEPT = "Accept";
    private static final String INVALID_MIME_TYPE = "Invalid mime type";
    private static final String MULTIPART_NOT_SUPPORTED = "Multipart requests are not supported";

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        // Reject multipart requests - this API only accepts JSON
        // Multipart requests are typically spam/bot traffic attempting to upload files
        var contentType = httpServletRequest.getContentType();
        if (contentType != null && contentType.toLowerCase().startsWith(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            httpServletResponse.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, MULTIPART_NOT_SUPPORTED);
            return;
        }

        // Validate Accept header MIME type
        var acceptHeader = httpServletRequest.getHeader(ACCEPT);
        if (StringUtils.isNotBlank(acceptHeader)) {
            try {
                MediaType.parseMediaTypes(acceptHeader);
            } catch (InvalidMediaTypeException e) {
                httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, INVALID_MIME_TYPE);
                return;
            }
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
