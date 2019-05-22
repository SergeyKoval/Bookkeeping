package by.bk.mail;

import org.springframework.stereotype.Component;

/**
 * @author Sergey Koval
 */
@Component
public class ExceptionNotificationEmailPreparator extends BaseEmailPreparator {
    private static final String SUBJECT = "Оповещение об ошибке";
    private static final String BODY = "<div><strong>Timestamp</strong>: %s</div>" +
                                       "<div><strong>User:</strong>: %s</div>" +
                                       "<div><strong>Exception:</strong>: %s</div>";

    @Override
    protected String getSubject() {
        return SUBJECT;
    }

    @Override
    protected String getBody() {
        return BODY;
    }
}