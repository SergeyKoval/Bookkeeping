package by.bk.entity.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Sergey Koval
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DeviceAssociation {
    private String sender;
    private String subAccountIdentifier;
}
