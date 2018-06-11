package by.bk.entity.history;

import by.bk.controller.model.response.SimpleResponse;

import java.util.List;

/**
 * @author Sergey Koval
 */
public interface HistoryAPI {
    List<HistoryItem> getPagePortion(String login, int page, int limit);
    SimpleResponse addHistoryItem(String login, HistoryItem historyItem);
}