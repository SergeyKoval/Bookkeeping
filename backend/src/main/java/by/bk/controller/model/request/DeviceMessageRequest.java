package by.bk.controller.model.request;

import by.bk.entity.history.DeviceMessage;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author Sergey Koval
 */
@Getter
@EqualsAndHashCode
public class DeviceMessageRequest {
    private String account;
    private String subAccount;
    private DeviceMessage deviceMessage;
}
