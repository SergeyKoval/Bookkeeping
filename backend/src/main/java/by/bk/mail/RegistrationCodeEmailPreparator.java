package by.bk.mail;

import org.springframework.stereotype.Component;

/**
 * @author Sergey Koval
 */
@Component
public class RegistrationCodeEmailPreparator extends BaseEmailPreparator {
    private static final String SUBJECT = "Код подтверждения регистрации";
    private static final String BODY = "<div>Код подтверждения: <strong>%s</strong></div>";

    @Override
    protected String getSubject() {
        return SUBJECT;
    }

    @Override
    protected String getBody() {
        return BODY;
    }
}