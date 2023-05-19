package by.bk.entity.history;

import by.bk.entity.user.model.DeviceSource;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Sergey Koval
 */
@Getter
@Setter
@EqualsAndHashCode
public class DeviceMessage {
    private String deviceId;
    private String sender;
    private String fullText;
    private Long messageTimestamp;
    private DeviceSource source;
}
