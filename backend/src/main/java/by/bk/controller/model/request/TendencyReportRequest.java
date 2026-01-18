package by.bk.controller.model.request;

import by.bk.entity.currency.Currency;
import lombok.Getter;

@Getter
public class TendencyReportRequest {
    private Currency currency;
    private DateRequest startPeriod;
    private DateRequest endPeriod;
}
