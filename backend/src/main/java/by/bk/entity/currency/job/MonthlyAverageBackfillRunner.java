package by.bk.entity.currency.job;

import by.bk.entity.currency.CurrencyAPI;
import by.bk.entity.currency.CurrencyDetail;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * One-time backfill runner to calculate monthly averages for all historical data.
 * DELETE THIS CLASS after running once locally.
 */
@Component
//public class MonthlyAverageBackfillRunner implements ApplicationRunner {
public class MonthlyAverageBackfillRunner {
    private static final Log LOG = LogFactory.getLog(MonthlyAverageBackfillRunner.class);

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private CurrencyAPI currencyAPI;

//    @Override
    public void run(ApplicationArguments args) {
        LOG.info("Starting monthly average backfill...");

        // Get all distinct year/month combinations where day is not null (daily rates only)
        var query = new Query(Criteria.where("day").ne(null));
        query.fields().include("year").include("month");

        var allDailyRates = mongoTemplate.find(query, CurrencyDetail.class);

        // Get distinct year/month pairs
        var distinctMonths = allDailyRates.stream()
                .map(cd -> new YearMonth(cd.getYear(), cd.getMonth()))
                .distinct()
                .sorted(Comparator.comparingInt(YearMonth::year).thenComparingInt(YearMonth::month))
                .collect(Collectors.toList());

        LOG.info("Found " + distinctMonths.size() + " months to process");

        var processed = 0;
        for (var ym : distinctMonths) {
            currencyAPI.recalculateMonthlyAverage(ym.year(), ym.month());
            processed++;
            if (processed % 12 == 0) {
                LOG.info("Processed " + processed + "/" + distinctMonths.size() + " months");
            }
        }

        LOG.info("Monthly average backfill complete: " + processed + " months processed");
    }

    private record YearMonth(int year, int month) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            var yearMonth = (YearMonth) o;
            return year == yearMonth.year && month == yearMonth.month;
        }

        @Override
        public int hashCode() {
            return Objects.hash(year, month);
        }
    }
}
