package by.bk.entity.history;

import by.bk.controller.model.response.SimpleResponse;
import by.bk.entity.currency.Currency;
import by.bk.entity.user.model.SubCategoryType;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author Sergey Koval
 */
public interface HistoryAPI {
    List<HistoryItem> getPagePortion(String login, int page, int limit);
    List<HistoryItem> getSuitable(String login, String category, String subCategory, SubCategoryType subCategoryType);
    SimpleResponse addHistoryItem(String login, HistoryItem historyItem, boolean changeGoalStatus);
    HistoryItem addBalanceHistoryItem(String login, Currency currency, String accountTitle, String subAccountTitle, Supplier<Double> value);
    SimpleResponse editHistoryItem(String login, HistoryItem historyItem, boolean changeGoalStatus, boolean changeOriginalGoalStatus);
    SimpleResponse deleteHistoryItem(String login, String historyItemId, boolean changeGoalStatus);
    HistoryItem getById(String login, String historyItemId);
}