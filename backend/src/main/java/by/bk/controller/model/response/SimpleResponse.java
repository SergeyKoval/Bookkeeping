package by.bk.controller.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Sergey Koval
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimpleResponse {
    private static final String ALREADY_EXIST = "ALREADY_EXIST";
    private static final String ERROR = "ERROR";
    private enum Status {
        SUCCESS, FAIL
    }

    private Status status;
    private String message;
    private Object result;

    private SimpleResponse(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public static SimpleResponse success() {
        return new SimpleResponse(Status.SUCCESS, null);
    }

    public static SimpleResponse success(String message) {
        return new SimpleResponse(Status.SUCCESS, message);
    }

    public static SimpleResponse fail(String message) {
        return new SimpleResponse(Status.FAIL, message);
    }

    public static SimpleResponse alreadyExistsFail() {
        return new SimpleResponse(Status.FAIL, ALREADY_EXIST);
    }

    public static SimpleResponse fail() {
        return new SimpleResponse(Status.FAIL, ERROR);
    }

    public static SimpleResponse failWithDetails(Object result) {
        SimpleResponse response = SimpleResponse.fail();
        response.setResult(result);
        return response;
    }

    @JsonIgnore
    public boolean isSuccess() {
        return Status.SUCCESS.equals(status);
    }
}