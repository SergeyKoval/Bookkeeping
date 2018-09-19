package by.bk.controller.model.request;

import by.bk.entity.history.HistoryItem;
import lombok.Getter;

/**
 * @author Sergey Koval
 */
@Getter
public class HistoryItemRequest {
    private String id;
    private HistoryItem historyItem;
    private boolean changeGoalStatus;
    private boolean changeOriginalGoalStatus;
}