package by.bk.controller.model.request;

import lombok.Getter;

/**
 * @author Sergey Koval
 */
@Getter
public class ChangeDeviceNameRequest {
    private String deviceId;
    private String name;
}
