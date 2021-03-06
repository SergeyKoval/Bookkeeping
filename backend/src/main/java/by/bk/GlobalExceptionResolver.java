package by.bk;

import by.bk.mail.EmailPreparator;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Sergey Koval
 */
@Component
public class GlobalExceptionResolver extends SimpleMappingExceptionResolver implements ErrorHandler {
    private static final Log LOG = LogFactory.getLog(GlobalExceptionResolver.class);
    private static final String REPLACE_PATTERN = System.getProperty("line.separator");
    private static final String REPLACEMENT = "<br>&emsp;";
    private static final String SYSTEM_USER = "SYSTEM";
    private static final List<Class> EXPECTED_EXCEPTIONS = List.of(BadCredentialsException.class);

    @Value("${mail.admin.username}")
    private String toEmail;
    @Value("${mail.exception.notify}")
    private boolean notify;
    @Autowired
    private EmailPreparator exceptionNotificationEmailPreparator;

    @PostConstruct
    public void init() {
        super.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (!EXPECTED_EXCEPTIONS.contains(ex.getClass())) {
            String stackTrace = RegExUtils.replaceAll(ExceptionUtils.getStackTrace(ex), REPLACE_PATTERN, REPLACEMENT);
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (notify) {
                exceptionNotificationEmailPreparator.prepareAndSend(toEmail, LocalDateTime.now(), principal, stackTrace);
            }
        }
        return super.doResolveException(request, response, handler, ex);
    }

    @Override
    public void handleError(Throwable t) {
        LOG.error(t.getMessage(), t);
        String stackTrace = RegExUtils.replaceAll(ExceptionUtils.getStackTrace(t), REPLACE_PATTERN, REPLACEMENT);
        if (notify) {
            exceptionNotificationEmailPreparator.prepareAndSend(toEmail, LocalDateTime.now(), SYSTEM_USER, stackTrace);
        }
    }
}