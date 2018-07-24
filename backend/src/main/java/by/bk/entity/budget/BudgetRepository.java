package by.bk.entity.budget;

import by.bk.entity.budget.model.Budget;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * @author Sergey Koval
 */
public interface BudgetRepository extends MongoRepository<Budget, ObjectId> {
    Optional<Budget> findByUserAndYearAndMonth(String user, Integer year, Integer month);
}