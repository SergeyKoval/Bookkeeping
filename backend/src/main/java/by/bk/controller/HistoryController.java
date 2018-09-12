package by.bk.controller;

import by.bk.controller.model.request.HistoryItemRequest;
import by.bk.controller.model.request.HistoryPageRequest;
import by.bk.controller.model.response.SimpleResponse;
import by.bk.entity.history.HistoryAPI;
import by.bk.entity.history.HistoryItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return historyAPI.getPagePortion(principal.getName(), request.getPage(), request.getLimit());
    }

    @PostMapping("/add")
    public SimpleResponse addHistoryItem(@RequestBody HistoryItemRequest request, Principal principal) {
        return historyAPI.addHistoryItem(principal.getName(), request.getHistoryItem(), request.isChangeGoalStatus());
    }

    @PostMapping("/edit")
    public SimpleResponse editHistoryItem(@RequestBody HistoryItem historyItem, Principal principal) {
        return historyAPI.editHistoryItem(principal.getName(), historyItem);
    }

    @PostMapping("/delete")
    public SimpleResponse deleteHistoryItem(@RequestBody HistoryItem historyItem, Principal principal) {
        return historyAPI.deleteHistoryItem(principal.getName(), historyItem.getId());
    }
}