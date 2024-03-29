package by.bk.controller.model.request;

import by.bk.entity.user.model.DeviceSource;
import lombok.Getter;

/**
 * @author Sergey Koval
 */
@Getter
public class SubAccountAssignmentRequest {
    private String account;
    private String subAccount;
    private String sender;
    private String subAccountIdentifier;
    private DeviceSource source;
}
