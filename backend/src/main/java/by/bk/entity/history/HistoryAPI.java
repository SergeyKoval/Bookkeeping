package by.bk.entity.history;

import by.bk.controller.model.response.SimpleResponse;
import by.bk.entity.currency.Currency;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author Sergey Koval
 */
public interface HistoryAPI {
    List<HistoryItem> getPagePortion(String login, int page, int limit);
    SimpleResponse addHistoryItem(String login, HistoryItem historyItem);
    HistoryItem addBalanceHistoryItem(String login, Currency currency, String accountTitle, String subAccountTitle, Supplier<Double> value);
    SimpleResponse editHistoryItem(String login, HistoryItem historyItem);
    SimpleResponse deleteHistoryItem(String login, String historyItemId);
}