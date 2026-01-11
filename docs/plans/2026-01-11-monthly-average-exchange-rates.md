# Monthly Average Exchange Rates Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add monthly average exchange rate calculation and storage alongside existing daily rates.

**Architecture:** Monthly averages stored in same `currencyDetails` collection with `day` field omitted. Averages recalculated after each daily rate fetch. One-time startup backfill for historical data.

**Tech Stack:** Java 17, Spring Boot, MongoDB, Spring Data

---

### Task 1: Add Repository Methods for Monthly Average Queries

**Files:**
- Modify: `backend/src/main/java/by/bk/entity/currency/CurrencyRepository.java:11-14`

**Step 1: Add repository method for daily rates only**

Add method to fetch daily rates (day is not null) for average calculation:

```java
List<CurrencyDetail> getByYearAndMonthAndDayIsNotNull(Integer year, Integer month);
```

**Step 2: Add repository method for monthly averages**

Add method to fetch monthly averages (day is null or missing):

```java
List<CurrencyDetail> getByYearAndMonthAndDayIsNull(Integer year, Integer month);
```

**Step 3: Verify compilation**

Run: `cd /Users/skoval/4work/Bookkeeping/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS

---

### Task 2: Add CurrencyAPI Interface Method

**Files:**
- Modify: `backend/src/main/java/by/bk/entity/currency/CurrencyAPI.java:9-13`

**Step 1: Add recalculateMonthlyAverage method to interface**

Add after line 12:

```java
void recalculateMonthlyAverage(int year, int month);
```

**Step 2: Verify compilation fails**

Run: `cd /Users/skoval/4work/Bookkeeping/backend && ./mvnw compile -q`
Expected: FAIL (CurrencyService doesn't implement the new method yet)

---

### Task 3: Implement recalculateMonthlyAverage in CurrencyService

**Files:**
- Modify: `backend/src/main/java/by/bk/entity/currency/CurrencyService.java:19-46`

**Step 1: Add import for EnumMap**

Add to imports section:

```java
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
```

**Step 2: Implement recalculateMonthlyAverage method**

Add after line 45 (before closing brace):

```java
@Override
public void recalculateMonthlyAverage(int year, int month) {
    List<CurrencyDetail> dailyRates = currencyRepository.getByYearAndMonthAndDayIsNotNull(year, month);
    if (dailyRates.isEmpty()) {
        return;
    }

    // Group daily rates by currency
    Map<Currency, List<CurrencyDetail>> ratesByCurrency = dailyRates.stream()
            .collect(Collectors.groupingBy(CurrencyDetail::getName));

    // Calculate and upsert average for each currency
    for (Currency currency : Currency.values()) {
        List<CurrencyDetail> currencyDailyRates = ratesByCurrency.get(currency);
        if (currencyDailyRates == null || currencyDailyRates.isEmpty()) {
            continue;
        }

        Map<Currency, Double> averageConversions = calculateAverageConversions(currencyDailyRates);
        upsertMonthlyAverage(year, month, currency, averageConversions);
    }
}

private Map<Currency, Double> calculateAverageConversions(List<CurrencyDetail> dailyRates) {
    Map<Currency, Double> sums = new EnumMap<>(Currency.class);
    Map<Currency, Integer> counts = new EnumMap<>(Currency.class);

    for (CurrencyDetail daily : dailyRates) {
        if (daily.getConversions() == null) {
            continue;
        }
        for (Map.Entry<Currency, Double> entry : daily.getConversions().entrySet()) {
            sums.merge(entry.getKey(), entry.getValue(), Double::sum);
            counts.merge(entry.getKey(), 1, Integer::sum);
        }
    }

    Map<Currency, Double> averages = new EnumMap<>(Currency.class);
    for (Map.Entry<Currency, Double> entry : sums.entrySet()) {
        Currency key = entry.getKey();
        double average = entry.getValue() / counts.get(key);
        // Round to 4 decimal places
        averages.put(key, Math.round(average * 10000.0) / 10000.0);
    }
    return averages;
}

private void upsertMonthlyAverage(int year, int month, Currency currency, Map<Currency, Double> conversions) {
    Query query = new Query(Criteria.where("year").is(year)
            .and("month").is(month)
            .and("name").is(currency)
            .and("day").is(null));

    Update update = new Update()
            .set("year", year)
            .set("month", month)
            .set("name", currency)
            .set("conversions", conversions);

    mongoTemplate.upsert(query, update, CurrencyDetail.class);
}
```

**Step 3: Add Update import**

Add to imports:

```java
import org.springframework.data.mongodb.core.query.Update;
```

**Step 4: Verify compilation**

Run: `cd /Users/skoval/4work/Bookkeeping/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS

---

### Task 4: Integrate Monthly Average Calculation into AlfaBankJob

**Files:**
- Modify: `backend/src/main/java/by/bk/entity/currency/job/AlfaBankJob.java:17-81`

**Step 1: Add recalculate call after storing daily rates**

Add after line 67 (after the last `addCurrencyDetail` call), before the closing brace of the if block:

```java
      currencyAPI.recalculateMonthlyAverage(today.getYear(), today.getMonthValue());
```

**Step 2: Verify compilation**

Run: `cd /Users/skoval/4work/Bookkeeping/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS

---

### Task 5: Integrate Monthly Average Calculation into NbrbJob

**Files:**
- Modify: `backend/src/main/java/by/bk/entity/currency/job/NbrbJob.java:28-130`

**Step 1: Add recalculate call after storing daily rates**

Add after line 94 (after the last `addCurrencyDetail` call), before the closing brace of the if block:

```java
                currencyAPI.recalculateMonthlyAverage(today.getYear(), today.getMonthValue());
```

**Step 2: Verify compilation**

Run: `cd /Users/skoval/4work/Bookkeeping/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS

---

### Task 6: Create One-Time Backfill Runner

**Files:**
- Create: `backend/src/main/java/by/bk/entity/currency/job/MonthlyAverageBackfillRunner.java`

**Step 1: Create the backfill runner class**

```java
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
public class MonthlyAverageBackfillRunner implements ApplicationRunner {
    private static final Log LOG = LogFactory.getLog(MonthlyAverageBackfillRunner.class);

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private CurrencyAPI currencyAPI;

    @Override
    public void run(ApplicationArguments args) {
        LOG.info("Starting monthly average backfill...");

        // Get all distinct year/month combinations where day is not null (daily rates only)
        Query query = new Query(Criteria.where("day").ne(null));
        query.fields().include("year").include("month");

        List<CurrencyDetail> allDailyRates = mongoTemplate.find(query, CurrencyDetail.class);

        // Get distinct year/month pairs
        List<YearMonth> distinctMonths = allDailyRates.stream()
                .map(cd -> new YearMonth(cd.getYear(), cd.getMonth()))
                .distinct()
                .sorted(Comparator.comparingInt(YearMonth::year).thenComparingInt(YearMonth::month))
                .collect(Collectors.toList());

        LOG.info("Found " + distinctMonths.size() + " months to process");

        int processed = 0;
        for (YearMonth ym : distinctMonths) {
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
            YearMonth yearMonth = (YearMonth) o;
            return year == yearMonth.year && month == yearMonth.month;
        }

        @Override
        public int hashCode() {
            return Objects.hash(year, month);
        }
    }
}
```

**Step 2: Verify compilation**

Run: `cd /Users/skoval/4work/Bookkeeping/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS

---

### Task 7: Write Unit Test for Monthly Average Calculation

**Files:**
- Create: `backend/src/test/java/by/bk/entity/currency/CurrencyServiceTest.java`

**Step 1: Create test class**

```java
package by.bk.entity.currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock
    private CurrencyRepository currencyRepository;
    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private CurrencyService currencyService;

    @Test
    void recalculateMonthlyAverage_shouldCalculateCorrectAverage() {
        // Given: 3 days of USD rates
        CurrencyDetail day1 = createDailyRate(Currency.USD, 2024, 1, 1, Map.of(Currency.BYN, 3.10, Currency.EUR, 0.92));
        CurrencyDetail day2 = createDailyRate(Currency.USD, 2024, 1, 2, Map.of(Currency.BYN, 3.20, Currency.EUR, 0.94));
        CurrencyDetail day3 = createDailyRate(Currency.USD, 2024, 1, 3, Map.of(Currency.BYN, 3.30, Currency.EUR, 0.96));

        when(currencyRepository.getByYearAndMonthAndDayIsNotNull(2024, 1))
                .thenReturn(List.of(day1, day2, day3));

        // When
        currencyService.recalculateMonthlyAverage(2024, 1);

        // Then: verify upsert was called with correct average
        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplate, times(1)).upsert(any(Query.class), updateCaptor.capture(), eq(CurrencyDetail.class));

        // Average BYN: (3.10 + 3.20 + 3.30) / 3 = 3.20
        // Average EUR: (0.92 + 0.94 + 0.96) / 3 = 0.94
        Update capturedUpdate = updateCaptor.getValue();
        assertNotNull(capturedUpdate);
    }

    @Test
    void recalculateMonthlyAverage_shouldHandleEmptyData() {
        // Given: no daily rates
        when(currencyRepository.getByYearAndMonthAndDayIsNotNull(2024, 1))
                .thenReturn(List.of());

        // When
        currencyService.recalculateMonthlyAverage(2024, 1);

        // Then: no upsert should be called
        verify(mongoTemplate, never()).upsert(any(Query.class), any(Update.class), eq(CurrencyDetail.class));
    }

    @Test
    void recalculateMonthlyAverage_shouldHandleMissingDays() {
        // Given: only 2 days of data (simulating missing days)
        CurrencyDetail day1 = createDailyRate(Currency.USD, 2024, 1, 1, Map.of(Currency.BYN, 3.10));
        CurrencyDetail day15 = createDailyRate(Currency.USD, 2024, 1, 15, Map.of(Currency.BYN, 3.30));

        when(currencyRepository.getByYearAndMonthAndDayIsNotNull(2024, 1))
                .thenReturn(List.of(day1, day15));

        // When
        currencyService.recalculateMonthlyAverage(2024, 1);

        // Then: should divide by 2, not by 15 or 31
        verify(mongoTemplate, times(1)).upsert(any(Query.class), any(Update.class), eq(CurrencyDetail.class));
    }

    private CurrencyDetail createDailyRate(Currency name, int year, int month, int day, Map<Currency, Double> conversions) {
        CurrencyDetail cd = new CurrencyDetail();
        cd.setName(name);
        cd.setYear(year);
        cd.setMonth(month);
        cd.setDay(day);
        cd.setConversions(conversions);
        return cd;
    }
}
```

**Step 2: Run tests**

Run: `cd /Users/skoval/4work/Bookkeeping/backend && ./mvnw test -Dtest=CurrencyServiceTest -q`
Expected: Tests pass

---

### Task 8: Full Build Verification

**Step 1: Run full build**

Run: `cd /Users/skoval/4work/Bookkeeping/backend && ./mvnw clean compile test -q`
Expected: BUILD SUCCESS

---

### Task 9: Manual Testing Instructions

After implementation, run locally:

1. Start the application
2. Watch logs for: "Starting monthly average backfill..."
3. Wait for: "Monthly average backfill complete: X months processed"
4. Verify in MongoDB:
   ```javascript
   db.currencyDetails.find({ day: null }).count()  // Should be ~400 (100 months × 4 currencies)
   db.currencyDetails.findOne({ day: null })  // Verify structure
   ```

---

### Task 10: Cleanup After Backfill

**After running locally and verifying data:**

1. Delete `MonthlyAverageBackfillRunner.java`
2. Keep all other changes
3. Ready for commit

---

## File Summary

| File | Action |
|------|--------|
| `CurrencyRepository.java` | Modify: add 2 query methods |
| `CurrencyAPI.java` | Modify: add interface method |
| `CurrencyService.java` | Modify: implement calculation logic |
| `AlfaBankJob.java` | Modify: call recalculate after daily insert |
| `NbrbJob.java` | Modify: call recalculate after daily insert |
| `MonthlyAverageBackfillRunner.java` | Create (temporary) |
| `CurrencyServiceTest.java` | Create |
