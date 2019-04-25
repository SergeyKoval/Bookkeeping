package by.bk.controller.model.response;

import by.bk.entity.currency.Currency;
import by.bk.entity.history.DynamicReportItem;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DynamicReportResponse {
    private int year;
    private int month;
    private String category;
    private String subCategory;
    private Currency currency;
    private Double value;

    public DynamicReportResponse(DynamicReportItem item, boolean ignoreSubCategory) {
        this.category = item.getCategory();
        this.year = item.getYear();
        this.month = item.getMonth();
        this.currency = item.getCurrency();
        this.value = item.getBalanceValue();
        if (!ignoreSubCategory) {
            this.subCategory = item.getSubCategory();
        }
    }

    public DynamicReportResponse(DynamicReportResponse baseItem) {
        this.category = baseItem.getCategory();
        this.year = baseItem.getYear();
        this.month = baseItem.getMonth();
        this.subCategory = baseItem.getSubCategory();
    }
}