# Monthly Average Exchange Rates Design

## Overview

Add monthly average exchange rate calculation to the existing daily exchange rate system. Monthly averages will be stored in the same `currencyDetails` MongoDB collection, distinguished by having no `day` field.

## Document Structure

**Daily rate document (existing):**
```json
{
  "name": "USD",
  "year": 2024,
  "month": 1,
  "day": 15,
  "conversions": { "BYN": 3.1234, "EUR": 0.9234, "RUB": 102.5678 }
}
```

**Monthly average document (new):**
```json
{
  "name": "USD",
  "year": 2024,
  "month": 1,
  "conversions": { "BYN": 3.1156, "EUR": 0.9198, "RUB": 101.8934 }
}
```

No `day` field indicates a monthly average. MongoDB queries for `day: null` match both explicit null and missing field.

## Calculation Logic

- **Formula:** `monthly_average = sum(daily_rates) / count(daily_documents)`
- Divide by actual number of documents available (handles missing days gracefully)
- Round to 4 decimal places (matching existing `DECIMAL_FORMAT`)
- Calculate for each currency (BYN, USD, EUR, RUB) and each conversion pair

## Implementation Changes

### Modified Files

1. **`CurrencyRepository.java`**
   - Add `getByYearAndMonthAndDayIsNotNull(year, month)` - fetch daily rates only
   - Add `getByYearAndMonthAndDayIsNull(year, month)` - fetch monthly averages

2. **`CurrencyAPI.java`**
   - Add `recalculateMonthlyAverage(int year, int month)` interface method

3. **`CurrencyService.java`**
   - Implement `recalculateMonthlyAverage(year, month)`:
     - Query all daily documents for the month (day is not null)
     - Group by currency
     - Calculate average for each conversion pair
     - Upsert monthly average documents (4 documents, one per currency)

4. **`NbrbJob.java`**
   - After storing daily rates, call `recalculateMonthlyAverage(currentYear, currentMonth)`

5. **`AlfaBankJob.java`**
   - After storing daily rates, call `recalculateMonthlyAverage(currentYear, currentMonth)`

### New Files (Temporary)

6. **`MonthlyAverageBackfillRunner.java`**
   - Implements `ApplicationRunner`
   - On startup: queries all distinct (year, month) pairs, calculates averages for each
   - Logs progress and completion
   - **Delete after running locally once**

## MongoDB Query Considerations

- `day: null` matches both `day: null` and missing `day` field
- Use `dayIsNotNull` queries when fetching daily data for calculations
- When storing monthly averages, omit the `day` field entirely (cleaner than explicit null)
- Upsert ensures idempotent operation (safe to re-run)

## Execution Plan

1. Implement repository methods
2. Implement service calculation method
3. Integrate with NbrbJob and AlfaBankJob
4. Create backfill runner
5. Run application locally to populate historical averages
6. Delete backfill runner
7. Commit remaining code

## Data Volume

- Current: ~11,284 daily documents (~7-8 years of data)
- Migration creates: ~400 monthly average documents (100 months × 4 currencies)
- Synchronous execution is sufficient for this volume
