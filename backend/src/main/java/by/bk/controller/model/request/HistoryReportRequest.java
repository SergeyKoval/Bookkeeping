package by.bk.controller.model.request;

import lombok.Getter;

import java.util.List;

@Getter
public class HistoryReportRequest {
    private DateRequest startPeriod;
    private DateRequest endPeriod;
    private List<List<String>> operations;
    private List<List<String>> accounts;
}