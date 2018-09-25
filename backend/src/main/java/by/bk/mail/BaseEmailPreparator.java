package by.bk.mail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.internet.MimeMessage;

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

    protected MimeMessage createMimeMessage() {
        return emailSender.createMimeMessage();
    }
}