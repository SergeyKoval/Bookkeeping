package by.bk.controller.model.request;

import by.bk.entity.history.Sms;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author Sergey Koval
 */
@Getter
@EqualsAndHashCode
public class SmsRequest {
    private String account;
    private String subAccount;
    private Sms sms;
}
