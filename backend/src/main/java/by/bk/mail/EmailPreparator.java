package by.bk.mail;

import jakarta.mail.internet.MimeMessage;

/**
 * @author Sergey Koval
 */
public interface EmailPreparator {
    String UTF_8 = "UTF-8";
    String FROM_ALIAS = "Домашняя бухгалтерия";

    MimeMessage prepare(String to, Object... substitutions) throws MessagePreparationException;
    boolean prepareAndSend(String to, Object... substitutions);
}
