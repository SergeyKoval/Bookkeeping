package by.bk.entity.history;

import by.bk.controller.model.request.AssignSmsRequest;
import by.bk.controller.model.request.DateRequest;
import by.bk.controller.model.request.DayProcessedHistoryItemsRequest;
import by.bk.controller.model.request.SmsRequest;
import by.bk.controller.model.response.DynamicReportResponse;
import by.bk.controller.model.response.SimpleResponse;
import by.bk.controller.model.response.SummaryReportResponse;
import by.bk.entity.currency.Currency;
import by.bk.entity.user.model.SubCategoryType;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Sergey Koval
 */
public interface HistoryAPI {
    List<HistoryItem> getPagePortion(String login, int page, int limit, boolean unprocessedSms);
    List<HistoryItem> getSuitable(String login, String category, String subCategory, SubCategoryType subCategoryType);
    SimpleResponse addHistoryItem(String login, HistoryItem historyItem, boolean changeGoalStatus);
    SimpleResponse addHistoryItemsFromSms(String login, String deviceId, List<SmsRequest> smsItems);
    HistoryItem addBalanceHistoryItem(String login, Currency currency, String accountTitle, String subAccountTitle, Supplier<Double> value);
    SimpleResponse editHistoryItem(String login, HistoryItem historyItem, boolean changeGoalStatus, boolean changeOriginalGoalStatus);
    SimpleResponse deleteHistoryItem(String login, String historyItemId, boolean changeGoalStatus);
    HistoryItem getById(String login, String historyItemId);
    SimpleResponse getDayProcessedHistoryItems(String login, DayProcessedHistoryItemsRequest request);

    List<HistoryItem> getFiltered(String login, DateRequest startPeriod, DateRequest endPeriod, List<List<String>> operations, List<List<String>> accounts);
    Collection<SummaryReportResponse> getPeriodSummary(String login, DateRequest startPeriod, DateRequest endPeriod, List<List<String>> operations, List<List<String>> accounts, List<String> currencies);
    Collection<DynamicReportResponse> getPeriodDynamic(String login, DateRequest startPeriod, DateRequest endPeriod, List<List<String>> operations, Currency currency);

    SimpleResponse getUnprocessedHistoryItemsCount(String login);
    SimpleResponse getDeviceSms(String login, String deviceId, Integer smsIndex);
    SimpleResponse assignSmsToHistoryItem(String login, AssignSmsRequest request);
}
