package by.bk.controller.model.request;

import lombok.Getter;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;

/**
 * @author Sergey Koval
 */
@Getter
public class DayProcessedHistoryItemsRequest {
    public enum Direction {
        previous {
            @Override
            public Sort.Order sort(String field) {
                return Sort.Order.desc(field);
            }

            @Override
            public Criteria filter(Criteria criteria, Object value) {
                return criteria.lt(value);
            }
        }, next {
            @Override
            public Sort.Order sort(String field) {
                return Sort.Order.asc(field);
            }

            @Override
            public Criteria filter(Criteria criteria, Object value) {
                return criteria.gt(value);
            }
        };

        public abstract Sort.Order sort(String field);
        public abstract Criteria filter(Criteria criteria, Object value);
    }

    private int year;
    private int month;
    private int day;
    private Direction direction;
}
