package by.bk.controller;

import by.bk.controller.model.request.*;
import by.bk.controller.model.response.SimpleResponse;
import by.bk.entity.history.HistoryAPI;
import by.bk.entity.history.HistoryItem;
import by.bk.security.role.RoleMobile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * @author Sergey Koval
 */
@RestController
@RequestMapping("/api/history")
public class HistoryController extends BaseAPIController {
    @Autowired
    private HistoryAPI historyAPI;

    @PostMapping("/page-portion")
    public List<HistoryItem> getPagePortion(@RequestBody HistoryPageRequest request, Principal principal) {
        return historyAPI.getPagePortion(principal.getName(), request.getPage(), request.getLimit(), request.isUnprocessedSms());
    }

    @PostMapping("/add")
    public SimpleResponse addHistoryItem(@RequestBody HistoryItemRequest request, Principal principal) {
        return historyAPI.addHistoryItem(principal.getName(), request.getHistoryItem(), request.isChangeGoalStatus());
    }

    @PostMapping("/edit")
    public SimpleResponse editHistoryItem(@RequestBody HistoryItemRequest request, Principal principal) {
        return historyAPI.editHistoryItem(principal.getName(), request.getHistoryItem(), request.isChangeGoalStatus(), request.isChangeOriginalGoalStatus());
    }

    @PostMapping("/delete")
    public SimpleResponse deleteHistoryItem(@RequestBody HistoryItemRequest request, Principal principal) {
        return historyAPI.deleteHistoryItem(principal.getName(), request.getId(), request.isChangeGoalStatus());
    }

    @RoleMobile
    @PostMapping("/sms")
    public SimpleResponse addHistoryItemsFromSms(@RequestBody List<SmsRequest> request, Principal principal) {
        return historyAPI.addHistoryItemsFromSms(principal.getName(), getDeviceId(principal), request);
    }

    @GetMapping("/devices/{deviceId}/sms/{index}")
    public SimpleResponse getDeviceSms(Principal principal, @PathVariable("deviceId") String deviceId, @PathVariable("index") Integer smsIndex) {
        return historyAPI.getDeviceSms(principal.getName(), deviceId, smsIndex);
    }

    @PostMapping("/day")
    public SimpleResponse getDayProcessedHistoryItems(@RequestBody DayProcessedHistoryItemsRequest request, Principal principal) {
        return historyAPI.getDayProcessedHistoryItems(principal.getName(), request);
    }

    @PostMapping("/assign-sms")
    public SimpleResponse assignSmsToHistoryItem(@RequestBody AssignSmsRequest request, Principal principal) {
        return historyAPI.assignSmsToHistoryItem(principal.getName(), request);
    }

    @GetMapping("/unprocessed-count")
    public SimpleResponse getUnprocessedHistoryItemsCount(Principal principal) {
        return historyAPI.getUnprocessedHistoryItemsCount(principal.getName());
    }
}
