package by.bk.mail;

import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;

/**
 * @author Sergey Koval
 */
@Component
public class RegistrationCodeEmailPreparator extends BaseEmailPreparator {
    private static final String SUBJECT = "Код подтверждения регистрации";
    private static final String BODY = "<div>Код подтверждения: <strong>%s</strong></div>";

    @Override
    public MimeMessage prepare(String to, Object... substitutions) throws MessagePreparationException {
        try {
            MimeMessage mimeMessage = createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, UTF_8);
            helper.setFrom(fromEmail, FROM_ALIAS);
            helper.setSubject(SUBJECT);
            helper.setTo(to);
            helper.setText(String.format(BODY, substitutions), true);
            return mimeMessage;
        } catch (MessagingException | UnsupportedEncodingException e) {
            LOG.error("Error preparing message: ", e);
            throw new MessagePreparationException();
        }
    }
}