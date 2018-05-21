package by.bk.controller.model.response;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Sergey Koval
 */
@Setter
@Getter
public class SimpleResponse {
    private enum Status {
        SUCCESS, FAIL
    }

    private Status status;
    private String message;

    private SimpleResponse(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public static SimpleResponse success() {
        return new SimpleResponse(Status.SUCCESS, null);
    }

    public static SimpleResponse fail(String message) {
        return new SimpleResponse(Status.FAIL, message);
    }
}