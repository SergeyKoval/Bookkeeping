package by.bk.entity.user.exception;

import by.bk.controller.model.response.SimpleResponse;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Sergey Koval
 */
public class SelectableItemMissedSettingUpdateException extends RuntimeException {
    private final String responseMessage;
    private final String errorMessage;

    public SelectableItemMissedSettingUpdateException(String errorMessage) {
        this.errorMessage = errorMessage;
        this.responseMessage = null;
    }

    public SelectableItemMissedSettingUpdateException(String errorMessage, String responseMessage) {
        this.responseMessage = responseMessage;
        this.errorMessage = errorMessage;
    }

    public SimpleResponse getSimpleResponse() {
        return StringUtils.isBlank(responseMessage) ? SimpleResponse.fail() : SimpleResponse.fail(responseMessage);
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}