package by.bk.entity.currency;

import by.bk.entity.user.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author Sergey Koval
 */
public interface CurrencyRepository extends MongoRepository<CurrencyDetail, String> {
    List<CurrencyDetail> getByYearAndMonthAndDay(Integer year, Integer month, Integer day);
}