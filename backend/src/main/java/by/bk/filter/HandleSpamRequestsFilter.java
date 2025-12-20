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
 * <p>
 * This filter also runs during ERROR dispatches to prevent multipart parsing
 * exceptions when Tomcat forwards to error pages.
 *
 * @author Sergey Koval
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HandleSpamRequestsFilter extends OncePerRequestFilter {

    /**
     * Also filter ERROR dispatches to block multipart requests during error page forwarding.
     * When an error occurs, Tomcat forwards to /error with the original request, which still
     * has the multipart content-type. Without filtering ERROR dispatches, Spring's
     * DispatcherServlet tries to parse the malformed multipart body and throws.
     */
    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    private static final String ACCEPT = "Accept";
    private static final String INVALID_MIME_TYPE = "Invalid mime type";
    private static final String MULTIPART_NOT_SUPPORTED = "Multipart requests are not supported";
    private static final String MULTIPART_PREFIX = "multipart/";

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        // Reject ALL multipart requests - this API only accepts JSON
        // Spring's StandardServletMultipartResolver processes any "multipart/*" content type,
        // not just "multipart/form-data", so we must block all multipart/* variants
        // (multipart/form-data, multipart/mixed, multipart/alternative, etc.)
        var contentType = httpServletRequest.getContentType();
        if (contentType != null && contentType.toLowerCase().startsWith(MULTIPART_PREFIX)) {
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
