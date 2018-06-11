package by.bk.entity.history;

import by.bk.controller.model.response.SimpleResponse;
import by.bk.entity.user.UserAPI;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * @author Sergey Koval
 */
@Service
public class HistoryService implements HistoryAPI {
    private static final Log LOG = LogFactory.getLog(HistoryService.class);

    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private UserAPI userAPI;

    @Override
    public List<HistoryItem> getPagePortion(String login, int page, int limit) {
        Query query = Query.query(Criteria.where("user").is(login))
                .with(Sort.by(Sort.Order.desc("year"), Sort.Order.desc("month"), Sort.Order.desc("day")))
                .skip((page -1) * limit)
                .limit(limit);

        return mongoTemplate.find(query, HistoryItem.class);
    }

    @Override
    public SimpleResponse addHistoryItem(String login, HistoryItem historyItem) {
        int order = 1 + historyRepository.getAllDayHistoryItemsWithOrder(login, historyItem.getYear(), historyItem.getMonth(), historyItem.getDay())
                .stream()
                .max(Comparator.comparingInt(HistoryItem::getOrder))
                .map(HistoryItem::getOrder)
                .orElse(0);

        historyItem.setOrder(order);
        historyItem.setUser(login);
        HistoryItem savedHistoryItem = historyRepository.save(historyItem);

        if (StringUtils.isBlank(savedHistoryItem.getId())) {
            LOG.error(StringUtils.join("Error adding history item ", savedHistoryItem, " for user ", login));
            return SimpleResponse.fail("ERROR");
        }

        SimpleResponse response = userAPI.updateUserBalance(login, savedHistoryItem.getType(), savedHistoryItem.getBalance());
        if (!response.isSuccess()) {
            LOG.error(StringUtils.join("Error updating user balance based on the history item ", savedHistoryItem, " for user ", login));
            historyRepository.delete(savedHistoryItem);
        }

        return response;
    }
}