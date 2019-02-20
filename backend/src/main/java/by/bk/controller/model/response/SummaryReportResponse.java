package by.bk.controller.model.response;

import by.bk.entity.currency.Currency;
import by.bk.entity.history.SummaryReportItem;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class SummaryReportResponse {
    private String category;
    private String subCategory;
    private Map<Currency, Double> values = new HashMap<>();

    public SummaryReportResponse(SummaryReportItem item, boolean ignoreSubCategory) {
        this.category = item.getCategory();
        this.values.put(item.getCurrency(), item.getBalanceValue());
        if (!ignoreSubCategory) {
            this.subCategory = item.getSubCategory();
        }
    }
}