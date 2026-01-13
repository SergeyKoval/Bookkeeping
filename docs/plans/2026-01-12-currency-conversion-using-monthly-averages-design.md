# Currency Conversion Using Monthly Averages

## Overview

Extend currency conversion functionality to use monthly average exchange rates in Budget and Report pages, while keeping the Summary panel using today's rates. A single currency dropdown in the Summary panel controls conversion across all pages.

## Architecture

### Single Dropdown, Multiple Conversion Strategies

The currency conversion dropdown in the Summary panel (left sidebar) is shared across all pages. When a user selects a conversion currency, it affects:

1. **Summary panel** - Uses today's rates (unchanged behavior)
2. **Budget page** - Uses selected month's average rates
3. **Summary Report page** - Uses end date's month average rates
4. **Actions Report page** - Uses actual rates from each history item's `alternativeCurrency` field
5. **Dynamic Report page** - No conversion (only chart, no values)

### Shared State Pattern

Currency selection is shared via `BehaviorSubject` in `CurrencyService`:

```typescript
private _conversionCurrency$: BehaviorSubject<CurrencyDetail> = new BehaviorSubject<CurrencyDetail>(null);
public conversionCurrency$: Observable<CurrencyDetail> = this._conversionCurrency$.asObservable();

public setConversionCurrency(currency: CurrencyDetail): void {
    this._conversionCurrency$.next(currency);
}

public get conversionCurrency(): CurrencyDetail {
    return this._conversionCurrency$.getValue();
}
```

Components subscribe to `conversionCurrency$` to react to changes.

## Conversion Logic by Area

| Area | Conversion Rate Source | Implementation |
|------|------------------------|----------------|
| Summary panel | Today's rates | `CurrencyService.convertToCurrency()` |
| Budget page | Selected month's average rate | `BudgetBalanceConversionPipe`, `BudgetGoalBalanceConversionPipe` |
| Summary Report | End date's month average rate | `SummaryValuesConversionPipe` |
| Actions Report | Actual rates from history item | `HistoryValueConversionPipe` |

## Backend Changes

### New Endpoint

`POST /api/currency/average-month-currencies`

**Request body:** `MonthCurrenciesRequest`
```java
{
  "year": 2026,
  "month": 1,
  "currencies": ["USD", "EUR", "BYN"]
}
```

**Response:** `List<CurrencyDetail>` - monthly average rates

### Fallback Logic

Implemented in `CurrencyService.getAverageCurrenciesForMonth()`:

1. Try requested year/month averages (documents with `day=null`)
2. If empty, try current month averages
3. If still empty, try previous month averages

This handles:
- Future month requested → falls back to current/previous
- Current month on day 1 before job runs → falls back to previous

### Backend Files Modified

- `CurrencyAPI.java` - added `getAverageCurrenciesForMonth()` interface method
- `CurrencyService.java` - implemented with fallback logic
- `CurrencyController.java` - added new endpoint

## Frontend Changes

### CurrencyService (`currency.service.ts`)

Added methods:
- `setConversionCurrency(currency)` - updates shared state
- `get conversionCurrency` - synchronous getter for current value
- `loadAverageCurrenciesForMonth(request)` - fetches and caches average rates
- `convertToAverageCurrency(value, from, to, year, month)` - converts using cached average rates

### New Pipes Created

#### `BudgetBalanceConversionPipe` (`budget-balance-conversion.pipe.ts`)
Converts budget balance objects using monthly average rates:
```typescript
transform(balance: {[currency: string]: BudgetBalance},
          conversionCurrency: CurrencyDetail,
          year: number,
          month: number): ConvertedBudgetBalance[]
```

#### `BudgetGoalBalanceConversionPipe` (`budget-goal-balance-conversion.pipe.ts`)
Converts goal balance objects using monthly average rates.

#### `HistoryValueConversionPipe` (`history-value-conversion.pipe.ts`)
Converts history item values using embedded `alternativeCurrency` map:
```typescript
transform(balance: HistoryBalanceType,
          conversionCurrency: CurrencyDetail): ConvertedHistoryValue
```
Falls back to original value if target currency not found in `alternativeCurrency`.

#### `SummaryValuesConversionPipe` (`summary-values-conversion.pipe.ts`)
Converts summary report values using monthly average rates:
```typescript
transform(values: {[currency: string]: number},
          conversionCurrency: CurrencyDetail,
          year: number,
          month: number): ConvertedSummaryValue[]
```

### Component Changes

#### `SummaryComponent` (`summary.component.ts`)
Updated `setSummaryConversion()` to also update shared state:
```typescript
public setSummaryConversion(currency: CurrencyDetail): void {
    this.conversionCurrency = currency;
    this._currencyService.setConversionCurrency(currency);
}
```

#### `BudgetComponent` (`budget.component.ts`)
- Subscribes to `conversionCurrency$`
- Loads average currencies when month changes or conversion is active
- Passes `conversionCurrency`, `year`, `month` to pipes in template

#### `BudgetDetailsComponent` (`budget-details.component.ts`)
- Receives `conversionCurrency` as input
- Fixed `getNumberOfCurrencies()` to return 1 when converting to single currency

#### `ReportActionsComponent` (`report-actions.component.ts`)
- Subscribes to `conversionCurrency$`
- Passes `conversionCurrency` to `HistoryValueConversionPipe` in template

#### `ReportSummaryComponent` (`report-summary.component.ts`)
- Subscribes to `conversionCurrency$`
- Stores `averageYear` and `averageMonth` from period filter's end date
- Loads average currencies when search is triggered with conversion active
- Passes parameters to `SummaryValuesConversionPipe` in template

### Angular Pipe Reactivity

Pure pipes only re-evaluate when input parameters change. To ensure reactivity:
1. `conversionCurrency` is passed as a parameter to all conversion pipes
2. Components subscribe to `conversionCurrency$` and trigger change detection
3. Year/month parameters are passed to pipes that need them

## Files Modified Summary

### Backend
- `bookkeeping-spring-boot/src/main/java/by/bk/entity/currency/CurrencyAPI.java`
- `bookkeeping-spring-boot/src/main/java/by/bk/entity/currency/CurrencyService.java`
- `bookkeeping-spring-boot/src/main/java/by/bk/controller/CurrencyController.java`

### Frontend
- `src/app/common/service/currency.service.ts`
- `src/app/summary/summary.component.ts`
- `src/app/budget/budget.component.ts`
- `src/app/budget/budget.component.html`
- `src/app/budget/budget-details/budget-details.component.ts`
- `src/app/budget/budget-details/budget-details.component.html`
- `src/app/reports/report-actions/report-actions.component.ts`
- `src/app/reports/report-actions/report-actions.component.html`
- `src/app/reports/report-summary/report-summary.component.ts`
- `src/app/reports/report-summary/report-summary.component.html`

### New Files
- `src/app/common/pipes/budget/budget-balance-conversion.pipe.ts`
- `src/app/common/pipes/budget/budget-goal-balance-conversion.pipe.ts`
- `src/app/common/pipes/history/history-value-conversion.pipe.ts`
- `src/app/common/pipes/report/summary-values-conversion.pipe.ts`
