package by.bk.entity.history;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Sergey Koval
 */
public interface HistoryRepository extends MongoRepository<HistoryItem, String> {

}