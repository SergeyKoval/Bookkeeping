package by.bk.controller;

import by.bk.controller.model.request.ReportRequest;
import by.bk.controller.model.response.DynamicReportResponse;
import by.bk.controller.model.response.SummaryReportResponse;
import by.bk.entity.history.HistoryAPI;
import by.bk.entity.history.HistoryItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/report")
public class ReportController {
    @Autowired
    private HistoryAPI historyAPI;

    @PostMapping("/history-actions")
    public List<HistoryItem> getHistoryActionsReport(@RequestBody ReportRequest request, Principal principal) {
        return historyAPI.getFiltered(principal.getName(), request.getStartPeriod(), request.getEndPeriod(), request.getOperations(), request.getAccounts(), request.getTags());
    }

    @PostMapping("/period-summary")
    public Collection<SummaryReportResponse> getPeriodSummaryReport(@RequestBody ReportRequest request, Principal principal) {
        return historyAPI.getPeriodSummary(principal.getName(), request.getStartPeriod(), request.getEndPeriod(), request.getOperations(), request.getAccounts(), request.getCurrencies(), request.getTags());
    }

    @PostMapping("/period-dynamic")
    public Collection<DynamicReportResponse> getPeriodDynamicReport(@RequestBody ReportRequest request, Principal principal) {
        return historyAPI.getPeriodDynamic(principal.getName(), request.getStartPeriod(), request.getEndPeriod(), request.getOperations(), request.getCurrency(), request.getTags());
    }
}
