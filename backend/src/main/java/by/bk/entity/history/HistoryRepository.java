package by.bk.entity.history;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * @author Sergey Koval
 */
public interface HistoryRepository extends MongoRepository<HistoryItem, String> {
    @Query(value = "{user: ?0, year: ?1, month: ?2, day: ?3}", fields = "{id: 1, order: 1}")
    List<HistoryItem> getAllDayHistoryItemsWithOrder(String login, int year, int month, int day);
    List<HistoryItem> getAllByUser(String user);
    List<HistoryItem> getAllByUserAndCategoryAndSubCategoryAndType(String user, String category, String subCategory, HistoryType type);
    @Query(value = "{user: ?0, year: ?1, month: ?2, day: ?3, notProcessed: {$ne: true}}", sort = "{order: -1}")
    List<HistoryItem> getProcessedHistoryItemsPerDay(String login, int year, int month, int day);
    Long countAllByNotProcessedTrueAndUser(String user);
}
