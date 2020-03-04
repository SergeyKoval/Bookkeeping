package by.bk.mail;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Sergey Koval
 */
@Component
public class ApplicationLinkEmailPreparator extends BaseEmailPreparator {
    private static final String SUBJECT = "Мобильное приложение Бухгалтерии";
    private static final String BODY_TEMPLATE = "<div>Мобильное приложение для отслеживания входящих SMS: <a href='%s'>%s</a></div>";

    @Value("${bookkeeper.host}")
    private String bookkeeperHost;
    private String body;

    @PostConstruct
    public void init() {
        String url = StringUtils.join(bookkeeperHost, "/mobile-app/android");
        body = String.format(BODY_TEMPLATE, url, url);
    }

    @Override
    protected String getSubject() {
        return SUBJECT;
    }

    @Override
    protected String getBody() {
        return body;
    }
}
