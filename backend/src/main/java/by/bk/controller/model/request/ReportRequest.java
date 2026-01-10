package by.bk.controller.model.request;

import by.bk.entity.currency.Currency;
import lombok.Getter;

import java.util.List;

@Getter
public class ReportRequest {
    private Currency currency;
    private DateRequest startPeriod;
    private DateRequest endPeriod;
    private List<List<String>> operations;
    private List<List<String>> accounts;
    private List<String> currencies;
    private List<String> tags;
}