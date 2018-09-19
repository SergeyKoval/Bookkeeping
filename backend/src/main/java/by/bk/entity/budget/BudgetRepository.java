package by.bk.entity.budget;

import by.bk.entity.budget.model.Budget;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author Sergey Koval
 */
public interface BudgetRepository extends MongoRepository<Budget, String> {
    Optional<Budget> findByUserAndYearAndMonth(String user, Integer year, Integer month);
    List<Budget> findAllByUser(String user);
}