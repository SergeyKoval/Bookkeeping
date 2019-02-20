package by.bk.entity.history;

import by.bk.entity.currency.Currency;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SummaryReportItem {
    private String category;
    private String subCategory;
    private Currency currency;
    private Double balanceValue;
}