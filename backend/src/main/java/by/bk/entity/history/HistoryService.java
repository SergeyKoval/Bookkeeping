package by.bk.entity.history;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Sergey Koval
 */
@Service
public class HistoryService implements HistoryAPI {
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<HistoryItem> getPagePortion(String login, int page, int limit) {
        Query query = Query.query(Criteria.where("user").is(login))
                .with(Sort.by(Sort.Order.desc("year"), Sort.Order.desc("month"), Sort.Order.desc("day")))
                .skip((page -1) * limit)
                .limit(limit);

        return mongoTemplate.find(query, HistoryItem.class);
    }
}