package by.bk.mail;

import org.springframework.stereotype.Component;

/**
 * @author Sergey Koval
 */
@Component
public class RestorePasswordEmailPreparator extends BaseEmailPreparator {
    @Override
    protected String getSubject() {
        return null;
    }

    @Override
    protected String getBody() {
        return null;
    }
}