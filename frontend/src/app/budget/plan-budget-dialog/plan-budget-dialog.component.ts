import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';

import { distinct, filter, map, scan } from 'rxjs/operators';
import { tap } from 'rxjs/internal/operators';
import { from, Observable, of } from 'rxjs';

import { ProfileService } from '../../common/service/profile.service';
import { LoadingService } from '../../common/service/loading.service';
import { LoadingDialogComponent } from '../../common/components/loading-dialog/loading-dialog.component';
import { BudgetService } from '../../common/service/budget.service';
import { CurrencyUtils } from '../../common/utils/currency-utils';
import { DateUtils } from '../../common/utils/date-utils';

@Component({
  selector: 'bk-plan-budget-dialog',
  templateUrl: './plan-budget-dialog.component.html',
  styleUrls: ['./plan-budget-dialog.component.css']
})
export class PlanBudgetDialogComponent implements OnInit {
  public months: string[] = DateUtils.MONTHS;
  public currencies: CurrencyDetail[];
  public errors: string;

  public selectedMonth: number;
  public selectedYear: number;
  public goalTitle: string;
  public budgetType: string;
  public categoryTitle: string;
  public typeCategories: Category[] = [];
  public currencyBalance: BudgetBalance[] = [{}];

  private _CATEGORIES: Category[];

  public constructor(
    @Inject(MAT_DIALOG_DATA) public data: {editMode: boolean, type: string, budgetType: string, category: BudgetCategory, budget: Budget},
    private _dialogRef: MatDialogRef<PlanBudgetDialogComponent>,
    private _profileService: ProfileService,
    private _loadingService: LoadingService,
    private _budgetService: BudgetService
  ) {}

  public ngOnInit(): void {
    this._CATEGORIES = this._profileService.authenticatedProfile.categories;
    this.currencies = this._profileService.authenticatedProfile.currencies;
    if (this.data.editMode) {
      this.budgetType = this.data.budgetType;
      if (this.data.category) {
        this.categoryTitle = this.data.category.title;
        of(this.data.category.balance).pipe(
          map(categoryBalance => Object.keys(categoryBalance).map(currency => {
            const currencyBalanceItem: BudgetBalance = Object.assign({}, categoryBalance[currency]);
            currencyBalanceItem.currency = currency;
            return currencyBalanceItem;
        }))).subscribe(value => this.currencyBalance = value);
      }
    }
  }

  public chooseMonth(monthIndex: number): void {
    if (!this.data.editMode && this.selectedMonth !== monthIndex) {
      this.selectedMonth = monthIndex;
    }
  }

  public getSelectedMonth(): string {
    return this.months[this.selectedMonth];
  }

  public decreaseYear(): void {
    if (!this.data.editMode) {
      this.selectedYear--;
      this.selectedMonth = 11;
    }
  }

  public increaseYear(): void {
    if (!this.data.editMode) {
      this.selectedYear++;
      this.selectedMonth = 0;
    }
  }

  public onChangeSelectedType(type: string): void {
    if (!this.data.editMode && type !== this.data.type) {
      this.data.type = type;
      this.budgetType = null;
      this.categoryTitle = null;
      this.goalTitle = null;
      this.errors = null;
      this.currencyBalance = [{}];
      this.typeCategories = [];
      if (this.data.type === 'goal') {
        const now: Date = new Date();
        this.selectedMonth = now.getMonth();
        this.selectedYear = now.getFullYear();
      }
    }
  }

  public changeBudgetType(type: string): void {
    if (!this.data.editMode && this.budgetType !== type) {
      this.budgetType = type;
      this.categoryTitle = null;
      this.goalTitle = null;
      this.currencyBalance = [{}];

      this.typeCategories = this._CATEGORIES
        .filter(category => category.subCategories.filter(subCategory => subCategory.type === this.budgetType).length > 0)
        .filter(category => this.data.type !== 'category' || !this.data.budget[this.budgetType].categories.map(budgetCategory => budgetCategory.title).includes(category.title));
    }
  }

  public changeCategory(category: Category): void {
    if (!this.data.editMode && category.title !== this.categoryTitle) {
      this.categoryTitle = category.title;
    }
  }

  public addCurrency(): void {
    if (this.currencyBalance.length < this.currencies.length) {
      this.currencyBalance.push({});
    }
  }

  public removeCurrency(balance: BudgetBalance): void {
    if (this.currencyBalance.length > 1) {
      this.currencyBalance.splice(this.currencyBalance.indexOf(balance), 1);
    }
  }

  public getBudgetTypeName(type: string): string {
    switch (type) {
      case 'income':
        return 'Доход';
      case 'expense':
        return 'Расход';
      default:
        return 'Тип';
    }
  }

  public close(refreshBudget: boolean): void {
    this._dialogRef.close(refreshBudget);
  }

  public getCategoryIcon(title: string): string {
    return this._profileService.getCategoryIcon(title);
  }

  public save(): void {
    let loadingDialog: MatDialogRef<LoadingDialogComponent>;
    this.errors = null;

    switch (this.data.type) {
      case 'category':
        if (this.validateCategory() === true) {
          loadingDialog = this.openLoadingFrame();
          if (this.data.editMode) {
            this.processResult(loadingDialog, this._budgetService.editBudgetCategory(this.data.budget.id, this.budgetType, this.categoryTitle, this.currencyBalance));
          } else {
            this.processResult(loadingDialog, this._budgetService.addBudgetCategory(this.data.budget.id, this.budgetType, this.categoryTitle, this.currencyBalance));
          }
        }
        break;
      case 'goal':
        if (this.validateGoal() === true) {
          loadingDialog = this.openLoadingFrame();
          if (this.data.editMode) {
            // this.processResult(loadingDialog, this._budgetService.editBudgetCategory(this.data.budget.id, this.budgetType, this.categoryTitle, this.currencyBalance));
          } else {
            this.processResult(loadingDialog, this._budgetService.addBudgetGoal(this.isSelectedMonth() ? this.data.budget.id : null, this.selectedYear,
              this.selectedMonth + 1, this.budgetType, this.categoryTitle, this.goalTitle, this.currencyBalance[0]));
          }
        }
        break;
      case 'limit':
        // result = this.validateLimit();
        break;
    }
  }

  private validateCategory(): boolean {
    if (!this.budgetType) {
      this.errors = 'Тип не выбран';
      return false;
    }

    if (!this.categoryTitle) {
      this.errors = 'Категория не выбрана';
      return false;
    }

    from(this.currencyBalance).pipe(
      map(balance => balance.currency),
      scan(([ dupes, uniques ], currency: string) => [uniques.has(currency) ? dupes.add(currency) : dupes, uniques.add(currency)], [new Set<string>(), new Set<string>()]),
      map(([ dupes ]) => dupes),
      filter(set => set.size > 0),
      distinct(),
      map(set => set.values().next().value)
    ).subscribe(currency => {
      this.errors = `Валюта ${CurrencyUtils.convertCodeToSymbol(this._profileService.getCurrencyDetails(currency).symbol)} задана более одного раза`;
    });

    const balanceValidation: boolean = this.validateBalance();
    if (balanceValidation && this.data.editMode) {
      const balanceMap: {} = this.currencyBalance.reduce((resultMap, balance) => {
        resultMap[balance.currency] = balance;
        return resultMap;
      }, {});
      const budgetBalance: BudgetBalance = this.data.category.balance;
      Object.keys(budgetBalance).forEach(currency => {
        if (!this.errors) {
          const usedValue: number = budgetBalance[currency].value;
          const planCurrency: BudgetBalance = balanceMap[currency];
          if (usedValue > 0 && (!planCurrency || planCurrency.completeValue < usedValue)) {
            this.errors = `Минимальное значение для ${CurrencyUtils.convertCodeToSymbol(this._profileService.getCurrencyDetails(currency).symbol)} = ${usedValue}`;
          }
        }
      });
    }

    return balanceValidation && !this.errors;
  }

  private validateGoal(): boolean {
    const now: Date = new Date();
    if (this.selectedYear < now.getFullYear() || (this.selectedYear === now.getFullYear() && this.selectedMonth < now.getMonth())) {
      this.errors = 'Цель для завершенного месяца';
      return false;
    }

    if (!this.budgetType) {
      this.errors = 'Тип не выбран';
      return false;
    }

    if (!this.categoryTitle) {
      this.errors = 'Категория не выбрана';
      return false;
    }

    if (!this.goalTitle) {
      this.errors = 'Название не задано';
      return false;
    }

    if (this.isSelectedMonth()) {
      const budgetSelectedCategory: BudgetCategory = this.data.budget[this.budgetType].categories.filter((category: BudgetCategory) => category.title === this.categoryTitle)[0];
      if (budgetSelectedCategory && budgetSelectedCategory.goals.filter(goal => goal.title === this.goalTitle).length > 0) {
        this.errors = 'Цель с таким названием уже существует';
        return false;
      }
    }

    const balanceValidation: boolean = this.validateBalance();
    if (balanceValidation && this.data.editMode) {

    }

    return balanceValidation;
  }

  private validateLimit(): boolean {
    return false;
  }

  private openLoadingFrame(): MatDialogRef<LoadingDialogComponent> {
    return this._loadingService.openLoadingDialog('Сохранение...');
  }

  private processResult(loadingDialog: MatDialogRef<LoadingDialogComponent>, result: Observable<SimpleResponse>): void {
    result.pipe(
      tap(simpleResponse => {
        loadingDialog.close();
        if (simpleResponse.status === 'FAIL') {
          this.errors = simpleResponse.message === 'ALREADY_EXISTS' ? 'Категория уже запланирована' : 'Ошибка при сохранении';
        }
      }),
      filter(simpleResponse => simpleResponse.status === 'SUCCESS'),
    ).subscribe(() => this._dialogRef.close(true));
  }

  private validateBalance(): boolean {
    return !this.errors && this.currencyBalance.filter(balance => {
      if (!balance.currency && !this.errors) {
        this.errors = 'Валюта не выбрана';
        return true;
      }

      if ((!balance.completeValue || balance.completeValue <= 0) && !this.errors) {
        this.errors = `Лимит для ${CurrencyUtils.convertCodeToSymbol(this._profileService.getCurrencyDetails(balance.currency).symbol)} не задан`;
        return true;
      }

      return false;
    }).length === 0;
  }

  private isSelectedMonth(): boolean {
    const budget: Budget = this.data.budget;
    return budget.year === this.selectedYear && budget.month === this.selectedMonth + 1;
  }
}
