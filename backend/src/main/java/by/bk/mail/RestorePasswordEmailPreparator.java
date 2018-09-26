package by.bk.mail;

import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

/**
 * @author Sergey Koval
 */
@Component
public class RestorePasswordEmailPreparator extends BaseEmailPreparator {
    @Override
    public MimeMessage prepare(String to, Object... substitutions) throws MessagePreparationException {
        return null;
    }
}