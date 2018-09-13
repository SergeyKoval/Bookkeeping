package by.bk.controller.model.request;

import lombok.Getter;

/**
 * @author Sergey Koval
 */
@Getter
public class HistoryItemDeleteRequest {
    private String id;
    private boolean changeGoalStatus;
}