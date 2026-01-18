# Tendency Report Implementation Plan

**Report Name:** Тенденции изменений
**URL:** `/reports/tendency`
**Version:** 3.4.7 -> 3.5.0

## Overview

This report shows the financial tendency (income - expense) over time based on budget data, with:
- Period filter (month/year only, no day selection)
- Single currency selector for conversion
- Line chart with green/red area fill based on value sign
- Summary table with income, expense, difference per month + totals

---

## Backend Changes

### 1. Request DTO

**File:** `backend/src/main/java/by/bk/controller/model/request/TendencyReportRequest.java`

```java
package by.bk.controller.model.request;

import by.bk.entity.currency.Currency;
import lombok.Getter;

@Getter
public class TendencyReportRequest {
    private Currency currency;
    private DateRequest startPeriod;
    private DateRequest endPeriod;
}
```

### 2. Response DTO

**File:** `backend/src/main/java/by/bk/controller/model/response/TendencyReportResponse.java`

```java
package by.bk.controller.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TendencyReportResponse {
    private int year;
    private int month;
    private double income;
    private double expense;
    private double difference;
}
```

### 3. BudgetAPI Interface

**File:** `backend/src/main/java/by/bk/entity/budget/BudgetAPI.java`

Add method:
```java
List<TendencyReportResponse> getTendencyReport(String login, DateRequest startPeriod, DateRequest endPeriod, Currency targetCurrency);
```

### 4. BudgetService Implementation

**File:** `backend/src/main/java/by/bk/entity/budget/BudgetService.java`

Add autowiring and implementation:

```java
@Override
public List<TendencyReportResponse> getTendencyReport(String login, DateRequest startPeriod, DateRequest endPeriod, Currency targetCurrency) {
    // Build period criteria once, reuse for both queries
    var periodCriteria = buildPeriodCriteria(startPeriod, endPeriod);

    // Query budgets
    var budgetCriteria = Criteria.where("user").is(login).orOperator(periodCriteria);
    var budgets = mongoTemplate.find(Query.query(budgetCriteria), Budget.class);

    // Query currency rates (day = null means monthly average)
    var ratesCriteria = Criteria.where("day").is(null).orOperator(periodCriteria);
    var allRates = mongoTemplate.find(Query.query(ratesCriteria), CurrencyDetail.class).stream()
        .collect(Collectors.groupingBy(
            rate -> rate.getYear() * 100 + rate.getMonth(),
            Collectors.toMap(CurrencyDetail::getName, rate -> rate, (a, b) -> a)
        ));

    return budgets.stream()
        .sorted(Comparator.comparingInt(Budget::getYear).thenComparingInt(Budget::getMonth))
        .map(budget -> buildTendencyResponse(budget, targetCurrency, allRates))
        .collect(Collectors.toList());
}

private TendencyReportResponse buildTendencyResponse(Budget budget, Currency targetCurrency, Map<Integer, Map<Currency, CurrencyDetail>> allRates) {
    var monthKey = budget.getYear() * 100 + budget.getMonth();
    var monthRates = allRates.getOrDefault(monthKey, Collections.emptyMap());
    var income = convertBalance(budget.getIncome().getBalance(), targetCurrency, monthRates);
    var expense = convertBalance(budget.getExpense().getBalance(), targetCurrency, monthRates);
    return new TendencyReportResponse(budget.getYear(), budget.getMonth(), income, expense, income - expense);
}

private Criteria[] buildPeriodCriteria(DateRequest startPeriod, DateRequest endPeriod) {
    var periodCriteria = new ArrayList<Criteria>();
    var year = startPeriod.getYear();
    var month = startPeriod.getMonth();
    while (year < endPeriod.getYear() || (year == endPeriod.getYear() && month <= endPeriod.getMonth())) {
        periodCriteria.add(Criteria.where("year").is(year).and("month").is(month));
        month++;
        if (month > 12) { month = 1; year++; }
    }
    return periodCriteria.toArray(new Criteria[0]);
}

private double convertBalance(Map<Currency, BalanceValue> balance, Currency targetCurrency, Map<Currency, CurrencyDetail> ratesMap) {
    if (balance == null || balance.isEmpty()) return 0.0;

    return balance.entrySet().stream()
        .mapToDouble(entry -> {
            var currency = entry.getKey();
            var value = entry.getValue().getValue() != null ? entry.getValue().getValue() : 0.0;

            if (currency == targetCurrency) {
                return value;
            }
            var rate = ratesMap.get(currency);
            if (rate != null && rate.getConversions() != null && rate.getConversions().containsKey(targetCurrency)) {
                return value * rate.getConversions().get(targetCurrency);
            }
            return 0.0;
        })
        .sum();
}
```

### 5. ReportController Endpoint

**File:** `backend/src/main/java/by/bk/controller/ReportController.java`

Add autowiring and endpoint:

```java
@Autowired
private BudgetAPI budgetAPI;

@PostMapping("/period-tendency")
public Collection<TendencyReportResponse> getPeriodTendencyReport(
    @RequestBody TendencyReportRequest request, Principal principal) {
    return budgetAPI.getTendencyReport(principal.getName(),
        request.getStartPeriod(), request.getEndPeriod(), request.getCurrency());
}
```

---

## Frontend Changes

### 1. Report Model

**File:** `frontend/src/app/common/model/report/tendency-report.ts`

```typescript
export interface TendencyReport {
  year: number;
  month: number;
  income: number;
  expense: number;
  difference: number;
}
```

### 2. Report Service Method

**File:** `frontend/src/app/common/service/report.service.ts`

Add import and method:

```typescript
import { TendencyReport } from '../model/report/tendency-report';

public getTendencyReport(currency: string, startYear: number, startMonth: number, endYear: number, endMonth: number): Observable<TendencyReport[]> {
  return this._http.post<TendencyReport[]>('/api/report/period-tendency', {
    'startPeriod': { year: startYear, month: startMonth },
    'endPeriod': { year: endYear, month: endMonth },
    'currency': currency
  });
}
```

### 3. Tendency Report Component

**File:** `frontend/src/app/reports/report-tendency/report-tendency.component.ts`

```typescript
import { Component, OnInit } from '@angular/core';
import { ChartDataset, ChartOptions, ScriptableContext } from 'chart.js';
import { ProfileService } from '../../common/service/profile.service';
import { AlertService } from '../../common/service/alert.service';
import { AlertType } from '../../common/model/alert/AlertType';
import { ReportService } from '../../common/service/report.service';
import { CurrencyDetail } from '../../common/model/currency-detail';
import { DateUtils } from '../../common/utils/date-utils';
import { TendencyReport } from '../../common/model/report/tendency-report';

@Component({
  selector: 'bk-report-tendency',
  templateUrl: './report-tendency.component.html',
  styleUrls: ['./report-tendency.component.css'],
  standalone: false
})
export class ReportTendencyComponent implements OnInit {
  public loading: boolean = false;
  public defaultCurrency: CurrencyDetail;
  public startYear: number;
  public startMonth: number;
  public endYear: number;
  public endMonth: number;

  public lineChartData: ChartDataset[] = [];
  public lineChartLabels: string[] = [];
  public lineChartOptions: ChartOptions = {
    responsive: true,
    scales: { y: { beginAtZero: true } }
  };

  public reportData: TendencyReport[] = [];
  public totals = { income: 0, expense: 0, difference: 0 };

  constructor(
    private _profileService: ProfileService,
    private _reportService: ReportService,
    private _alertService: AlertService
  ) {
    const now = new Date();
    this.startYear = now.getFullYear();
    this.startMonth = 1;
    this.endYear = now.getFullYear();
    this.endMonth = now.getMonth() + 1;
  }

  ngOnInit(): void {
    this.defaultCurrency = this._profileService.defaultCurrency;
  }

  changeCurrency(currency: CurrencyDetail): void {
    this.defaultCurrency = currency;
  }

  onStartPeriodChange(event: { year: number; month: number }): void {
    this.startYear = event.year;
    this.startMonth = event.month;
  }

  onEndPeriodChange(event: { year: number; month: number }): void {
    this.endYear = event.year;
    this.endMonth = event.month;
  }

  search(): void {
    if (!this.defaultCurrency) {
      this._alertService.addAlert(AlertType.WARNING, 'Валюта не выбрана');
      return;
    }

    this.loading = true;
    this._reportService.getTendencyReport(
      this.defaultCurrency.name,
      this.startYear, this.startMonth,
      this.endYear, this.endMonth
    ).subscribe(items => {
      this.reportData = items;
      this.buildChart(items);
      this.calculateTotals(items);
      this.loading = false;
    });
  }

  private buildChart(items: TendencyReport[]): void {
    this.lineChartLabels = items.map(item =>
      `${DateUtils.MONTHS[item.month - 1]} ${item.year}`
    );

    const differences = items.map(item => item.difference);

    this.lineChartData = [{
      label: 'Разница (доход - расход)',
      data: differences,
      fill: {
        target: 'origin',
        above: 'rgba(75, 192, 92, 0.3)',
        below: 'rgba(255, 99, 132, 0.3)'
      },
      borderColor: (ctx: ScriptableContext<'line'>) => {
        const value = ctx.raw as number;
        return value >= 0 ? 'rgb(75, 192, 92)' : 'rgb(255, 99, 132)';
      },
      segment: {
        borderColor: (ctx) => {
          const value = ctx.p1.parsed.y;
          return value >= 0 ? 'rgb(75, 192, 92)' : 'rgb(255, 99, 132)';
        }
      },
      tension: 0.1
    }];
  }

  private calculateTotals(items: TendencyReport[]): void {
    this.totals = items.reduce((acc, item) => ({
      income: acc.income + item.income,
      expense: acc.expense + item.expense,
      difference: acc.difference + item.difference
    }), { income: 0, expense: 0, difference: 0 });
  }
}
```

**File:** `frontend/src/app/reports/report-tendency/report-tendency.component.html`

```html
<div class="panel panel-default">
  <div class="panel-heading text-center">Тенденции изменений</div>
  <div class="panel-body">
    <div class="row filter-row">
      <div class="col-sm-11 filters">
        <div class="filter period-selector">
          <span>С:</span>
          <bk-month-and-year
            [selectedYear]="startYear"
            [selectedMonth]="startMonth"
            (changeYear)="onStartPeriodChange($event)"
            (changeMonth)="startMonth = $event">
          </bk-month-and-year>
        </div>
        <div class="filter period-selector">
          <span>По:</span>
          <bk-month-and-year
            [selectedYear]="endYear"
            [selectedMonth]="endMonth"
            (changeYear)="onEndPeriodChange($event)"
            (changeMonth)="endMonth = $event">
          </bk-month-and-year>
        </div>
        <bk-currency-conversion
          label="К валюте:"
          denyNoChoice="true"
          [selectedCurrency]="defaultCurrency"
          class="filter"
          (currencyConversion)="changeCurrency($event)">
        </bk-currency-conversion>
      </div>
      <div class="col-sm-1">
        <span class="btn btn-default search-button pull-right" (click)="search()">Поиск</span>
      </div>
    </div>

    @if (!loading) {
      <div>
        <!-- Chart -->
        <div class="row">
          <div class="col-sm-12">
            @if (lineChartData.length > 0) {
              <canvas baseChart
                [datasets]="lineChartData"
                [labels]="lineChartLabels"
                [options]="lineChartOptions"
                chartType="line">
              </canvas>
            }
          </div>
        </div>

        <!-- Summary Table -->
        @if (reportData.length > 0) {
          <div class="row" style="margin-top: 20px;">
            <div class="col-sm-12">
              <table class="table table-striped table-bordered">
                <thead>
                  <tr>
                    <th>Период</th>
                    <th class="text-right">Доход</th>
                    <th class="text-right">Расход</th>
                    <th class="text-right">Разница</th>
                  </tr>
                </thead>
                <tbody>
                  @for (item of reportData; track item) {
                    <tr>
                      <td>{{ item.month }}.{{ item.year }}</td>
                      <td class="text-right">{{ item.income | number:'1.2-2' }}</td>
                      <td class="text-right">{{ item.expense | number:'1.2-2' }}</td>
                      <td class="text-right" [class.text-success]="item.difference >= 0" [class.text-danger]="item.difference < 0">
                        {{ item.difference | number:'1.2-2' }}
                      </td>
                    </tr>
                  }
                </tbody>
                <tfoot>
                  <tr class="active">
                    <th>Итого</th>
                    <th class="text-right">{{ totals.income | number:'1.2-2' }}</th>
                    <th class="text-right">{{ totals.expense | number:'1.2-2' }}</th>
                    <th class="text-right" [class.text-success]="totals.difference >= 0" [class.text-danger]="totals.difference < 0">
                      {{ totals.difference | number:'1.2-2' }}
                    </th>
                  </tr>
                </tfoot>
              </table>
            </div>
          </div>
        }
      </div>
    } @else {
      <div class="spinner-wrapper"><bk-spinner [size]="100"></bk-spinner></div>
    }
  </div>
</div>
```

**File:** `frontend/src/app/reports/report-tendency/report-tendency.component.css`

```css
.filter-row {
  margin-bottom: 15px;
}

.filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.filter {
  display: inline-block;
}

.period-selector {
  display: flex;
  align-items: center;
  gap: 5px;
}

.spinner-wrapper {
  display: flex;
  justify-content: center;
  padding: 50px;
}

.text-success {
  color: #5cb85c;
}

.text-danger {
  color: #d9534f;
}
```

### 4. Routes Update

**File:** `frontend/src/app/routes.ts`

Add import:
```typescript
import { ReportTendencyComponent } from './reports/report-tendency/report-tendency.component';
```

Add route to reports children (after dynamic):
```typescript
{
  path: 'tendency',
  component: ReportTendencyComponent,
  data: {title: 'Бухгалтерия - отчеты - тенденции изменений'}
}
```

### 5. Module Update

**File:** `frontend/src/app/bk.module.ts`

Add import:
```typescript
import { ReportTendencyComponent } from './reports/report-tendency/report-tendency.component';
```

Add to declarations array:
```typescript
ReportTendencyComponent,
```

### 6. Navigation Update

**File:** `frontend/src/app/bk/header/header.component.html`

Add menu item (after "Динамика за период"):
```html
<li routerLinkActive="active"><a [routerLink]="['reports', 'tendency']">Тенденции изменений</a></li>
```

---

## Version Update

### Backend
**File:** `backend/pom.xml`

Change line 13:
```xml
<version>3.5.0</version>
```

### Frontend
**File:** `frontend/package.json`

Change line 3:
```json
"version": "3.5.0",
```

---

## Files Summary

### New Files
| File | Description |
|------|-------------|
| `backend/.../request/TendencyReportRequest.java` | Request DTO |
| `backend/.../response/TendencyReportResponse.java` | Response DTO |
| `frontend/.../model/report/tendency-report.ts` | TypeScript model |
| `frontend/.../report-tendency/report-tendency.component.ts` | Component logic |
| `frontend/.../report-tendency/report-tendency.component.html` | Template |
| `frontend/.../report-tendency/report-tendency.component.css` | Styles |

### Modified Files
| File | Changes |
|------|---------|
| `backend/pom.xml` | Version 3.4.7 -> 3.5.0 |
| `backend/.../BudgetAPI.java` | Add method signature |
| `backend/.../BudgetService.java` | Add CurrencyAPI, implement method |
| `backend/.../ReportController.java` | Add BudgetAPI, add endpoint |
| `frontend/package.json` | Version 3.4.7 -> 3.5.0 |
| `frontend/.../report.service.ts` | Add method |
| `frontend/.../routes.ts` | Add route |
| `frontend/.../bk.module.ts` | Add declaration |
| `frontend/.../header.component.html` | Add nav item |

---

## Verification Checklist

- [ ] Backend compiles: `cd backend && mvn compile`
- [ ] Frontend compiles: `cd frontend && npm run build`
- [ ] Navigate to `/reports/tendency`
- [ ] Select period and currency
- [ ] Chart shows green fill for positive values
- [ ] Chart shows red fill for negative values
- [ ] Table shows income, expense, difference per month
- [ ] Totals row is correct
- [ ] Navigation menu shows link
