package by.bk;

import by.bk.mail.EmailPreparator;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

/**
 * @author Sergey Koval
 */
@Component
public class GlobalExceptionResolver extends SimpleMappingExceptionResolver implements ErrorHandler {
    private static final Log LOG = LogFactory.getLog(GlobalExceptionResolver.class);
    private static final String REPLACE_PATTERN = "\\r\\n\\t";
    private static final String REPLACEMENT = "<br>&emsp;";
    private static final String SYSTEM_USER = "SYSTEM";

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
//        String stackTrace = RegExUtils.replaceAll(ExceptionUtils.getStackTrace(ex), REPLACE_PATTERN, REPLACEMENT);
        String stackTrace = ExceptionUtils.getStackTrace(ex);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (notify) {
            exceptionNotificationEmailPreparator.prepareAndSend(toEmail, LocalDateTime.now(), principal, stackTrace);
        }
        return super.doResolveException(request, response, handler, ex);
    }

    @Override
    public void handleError(Throwable t) {
        LOG.error(t.getMessage(), t);
//        String stackTrace = RegExUtils.replaceAll(ExceptionUtils.getStackTrace(t), REPLACE_PATTERN, REPLACEMENT);
        String stackTrace = ExceptionUtils.getStackTrace(t);
        if (notify) {
            exceptionNotificationEmailPreparator.prepareAndSend(toEmail, LocalDateTime.now(), SYSTEM_USER, stackTrace);
        }
    }
}