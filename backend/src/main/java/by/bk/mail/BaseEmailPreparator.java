package by.bk.mail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;

/**
 * @author Sergey Koval
 */
public abstract class BaseEmailPreparator implements EmailPreparator {
    protected final Log LOG = LogFactory.getLog(this.getClass());

    @Value("${spring.mail.username}")
    protected String fromEmail;
    @Autowired
    protected JavaMailSender emailSender;

    @Override
    public boolean prepareAndSend(String to, Object... substitutions) {
        try {
            MimeMessage mimeMessage = prepare(to, substitutions);
            emailSender.send(mimeMessage);
            return true;
        } catch (Exception e) {
            LOG.error("Error sending message: ", e);
            return false;
        }
    }

    @Override
    public MimeMessage prepare(String to, Object... substitutions) throws MessagePreparationException {
        try {
            MimeMessage mimeMessage = createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, UTF_8);
            helper.setFrom(fromEmail, FROM_ALIAS);
            helper.setSubject(getSubject());
            helper.setTo(to);
            helper.setText(String.format(getBody(), substitutions), true);
            return mimeMessage;
        } catch (MessagingException | UnsupportedEncodingException e) {
            LOG.error("Error preparing message: ", e);
            throw new MessagePreparationException();
        }
    }

    protected MimeMessage createMimeMessage() {
        return emailSender.createMimeMessage();
    }

    protected abstract String getSubject();
    protected abstract String getBody();
}