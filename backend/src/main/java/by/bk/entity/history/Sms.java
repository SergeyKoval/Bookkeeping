package by.bk.entity.history;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Sergey Koval
 */
@Getter
@Setter
@EqualsAndHashCode
public class Sms {
    private String deviceId;
    private String sender;
    private String fullSms;
    private Long smsTimestamp;
}
