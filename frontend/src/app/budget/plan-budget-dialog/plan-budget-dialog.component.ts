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
import { ConfirmDialogService } from '../../common/components/confirm-dialog/confirm-dialog.service';

@Component({
  selector: 'bk-plan-budget-dialog',
  templateUrl: './plan-budget-dialog.component.html',
  styleUrls: ['./plan-budget-dialog.component.css']
})
export class PlanBudgetDialogComponent implements OnInit {
  public currencies: CurrencyDetail[];
  public errors: string;
  public showDetails: boolean = false;

  public changeGoalMonthAvailable: boolean = true;
  public selectedMonth: number;
  public selectedYear: number;
  public goalTitle: string;
  public budgetType: string;
  public categoryTitle: string;
  public typeCategories: Category[] = [];
  public currencyBalance: BudgetBalance[] = [{}];

  private _CATEGORIES: Category[];

  public constructor(
    @Inject(MAT_DIALOG_DATA) public data: {editMode: boolean, type: string, budgetType: string, category: BudgetCategory, budget: Budget, goal: BudgetGoal},
    private _dialogRef: MatDialogRef<PlanBudgetDialogComponent>,
    private _profileService: ProfileService,
    private _loadingService: LoadingService,
    private _budgetService: BudgetService,
    private _confirmDialogService: ConfirmDialogService
  ) {}

  public ngOnInit(): void {
    this._CATEGORIES = this._profileService.authenticatedProfile.categories;
    this.currencies = this._profileService.authenticatedProfile.currencies;
    this.selectedMonth = this.data.budget.month;
    this.selectedYear = this.data.budget.year;

    if (this.data.editMode) {
      this.budgetType = this.data.budgetType;
      this.categoryTitle = this.data.category.title;

      if (this.data.goal) {
        this.goalTitle = this.data.goal.title;
        this.changeGoalMonthAvailable = this.data.goal.balance.value === 0;
        this.currencyBalance = [Object.assign({}, this.data.goal.balance)];
      } else {
        of(this.data.category.balance).pipe(
          map(categoryBalance => Object.keys(categoryBalance).map(currency => {
            const currencyBalanceItem: BudgetBalance = Object.assign({}, categoryBalance[currency]);
            currencyBalanceItem.currency = currency;
            return currencyBalanceItem;
        }))).subscribe(value => this.currencyBalance = value);
      }
    }
  }

  public changeMonth(monthIndex: number): void {
    if (this.selectedMonth !== monthIndex && this.changeGoalMonthAvailable) {
      this.selectedMonth = monthIndex;
    }
  }

  public changeYear(date: {year: number, month: number}): void {
    if (this.changeGoalMonthAvailable) {
      this.selectedYear = date.year;
      this.selectedMonth = date.month;
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
    }
  }

  public changeBudgetType(type: string): void {
    if (!this.data.editMode && this.budgetType !== type) {
      this.budgetType = type;

      if (this.data.type === 'limit') {
        of(this.data.budget[this.budgetType].balance).pipe(
          map(categoryBalance => Object.keys(categoryBalance).map(currency => {
            const currencyBalanceItem: BudgetBalance = Object.assign({}, categoryBalance[currency]);
            currencyBalanceItem.currency = currency;
            return currencyBalanceItem;
          }))).subscribe(value => this.currencyBalance = value.length > 0 ? value : [{}]);
      } else {
        this.categoryTitle = null;
        this.goalTitle = null;
        this.currencyBalance = [{}];

        this.typeCategories = this._CATEGORIES
          .filter(category => category.subCategories.filter(subCategory => subCategory.type === this.budgetType).length > 0)
          .filter(category => this.data.type !== 'category' || !this.data.budget[this.budgetType].categories.map(budgetCategory => budgetCategory.title).includes(category.title));
      }
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
    this.showDetails = false;

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
            if (this.currencyBalance[0].value === this.currencyBalance[0].completeValue && !this.data.goal.done) {
              this._confirmDialogService.openConfirmDialog('Изменение статуса цели', 'Цель выполнена?')
                .afterClosed()
                .subscribe((result: boolean) => this.processResult(loadingDialog, this._budgetService.editBudgetGoal(this.data.budget.id, this.selectedYear,
                  this.selectedMonth, this.budgetType, this.categoryTitle, this.data.goal.title, this.goalTitle, this.currencyBalance[0], result)));
            } else {
              const changeGoalState: boolean = this.data.goal.done
                && this.currencyBalance[0].value < this.currencyBalance[0].completeValue
                && this.currencyBalance[0].completeValue !== this.data.goal.balance.completeValue;
              this.processResult(loadingDialog, this._budgetService.editBudgetGoal(this.data.budget.id, this.selectedYear,
                this.selectedMonth, this.budgetType, this.categoryTitle, this.data.goal.title, this.goalTitle, this.currencyBalance[0], changeGoalState));
            }
          } else {
            this.processResult(loadingDialog, this._budgetService.addBudgetGoal(this.isSelectedMonth() ? this.data.budget.id : null, this.selectedYear,
              this.selectedMonth, this.budgetType, this.categoryTitle, this.goalTitle, this.currencyBalance[0]));
          }
        }
        break;
      case 'limit':
        if (this.validateLimit() === true) {
          loadingDialog = this.openLoadingFrame();
          this.processResult(loadingDialog, this._budgetService.updateBudgetLimit(this.data.budget.id, this.budgetType, this.currencyBalance));
        }
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

    this.validateDuplicateCurrencies();
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
          if (usedValue > 0 && (!planCurrency || planCurrency.completeValue < usedValue) && planCurrency.confirmValue !== true) {
            this.errors = `Минимальное значение для ${CurrencyUtils.convertCodeToSymbol(this._profileService.getCurrencyDetails(currency).symbol)} = ${usedValue}`;
            planCurrency.showConfirm = true;
            planCurrency.confirmValue = false;
            this.showDetails = true;
          }
        }
      });
    }

    return balanceValidation && !this.errors;
  }

  private validateGoal(): boolean {
    const now: Date = new Date();
    if (this.selectedYear < now.getFullYear() || (this.selectedYear === now.getFullYear() && this.selectedMonth < now.getMonth() + 1)) {
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
      if (budgetSelectedCategory) {
        const goalNameExists: boolean = budgetSelectedCategory.goals.filter(goal => goal.title === this.goalTitle).length > 0;
        if ((!this.data.editMode && goalNameExists) || (this.data.editMode && goalNameExists && this.goalTitle !== this.data.goal.title)) {
          this.errors = 'Цель с таким названием уже существует';
          return false;
        }
      }
    }

    const balanceValidation: boolean = this.validateBalance();
    if (balanceValidation && this.data.editMode) {
      const budgetBalance: BudgetBalance = this.currencyBalance[0];
      const dataBalance: BudgetBalance = this.data.goal.balance;
      if (budgetBalance.value > budgetBalance.completeValue && budgetBalance.completeValue !== dataBalance.completeValue
        && budgetBalance.currency === dataBalance.currency && budgetBalance.confirmValue !== true) {

        this.errors = `Минимальное значение для ${CurrencyUtils.convertCodeToSymbol(this._profileService.getCurrencyDetails(budgetBalance.currency).symbol)} = ${budgetBalance.value}`;
        budgetBalance.showConfirm = true;
        budgetBalance.confirmValue = false;
        this.showDetails = true;
        return false;
      }
    }

    return balanceValidation;
  }

  private validateLimit(): boolean {
    if (!this.budgetType) {
      this.errors = 'Тип не выбран';
      return false;
    }

    this.validateDuplicateCurrencies();
    const balanceValidation: boolean = this.validateBalance();
    if (balanceValidation) {
      const balanceMap: {} = this.currencyBalance.reduce((resultMap, balance) => {
        resultMap[balance.currency] = balance;
        return resultMap;
      }, {});

      const minimumBalanceMap: Map<string, number> = new Map<string, number>();
      this.data.budget[this.budgetType].categories.forEach((category: BudgetCategory) => {
        const categoryBalance: {[currency: string]: BudgetBalance} = category.balance;
        Object.keys(categoryBalance).forEach(currency => {
          const minBalance: BudgetBalance = categoryBalance[currency];
          const minValue: number = minBalance.completeValue > minBalance.value ? minBalance.completeValue : minBalance.value;
          minimumBalanceMap.set(currency, minimumBalanceMap.has(currency) ? (minimumBalanceMap.get(currency) + minValue) : minValue);
        });
      });

      minimumBalanceMap.forEach((minimumValue, currency) => {
        if (!this.errors) {
          const planedValue: BudgetBalance = balanceMap[currency];
          if (!planedValue || (planedValue.completeValue < minimumValue && planedValue.confirmValue !== true)) {
            this.errors = `Минимальное значение для ${CurrencyUtils.convertCodeToSymbol(this._profileService.getCurrencyDetails(currency).symbol)} = ${minimumValue}`;
            planedValue.showConfirm = true;
            planedValue.confirmValue = false;
            this.showDetails = true;
          }
        }
      });
    }

    return balanceValidation && !this.errors;
  }

  private validateDuplicateCurrencies(): void {
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
  }

  private openLoadingFrame(): MatDialogRef<LoadingDialogComponent> {
    return this._loadingService.openLoadingDialog('Сохранение...');
  }

  private processResult(loadingDialog: MatDialogRef<LoadingDialogComponent>, result: Observable<SimpleResponse>): void {
    result.pipe(
      tap(simpleResponse => {
        loadingDialog.close();
        if (simpleResponse.status === 'FAIL') {
          this.errors = simpleResponse.message === 'ALREADY_EXIST' ? 'Категория уже запланирована' : 'Ошибка при сохранении';
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

      if (balance.value && !balance.completeValue) {
        balance.completeValue = 0;
      }

      if ((!balance.value && (!balance.completeValue || balance.completeValue <= 0)) && !this.errors) {
        this.errors = `Лимит для ${CurrencyUtils.convertCodeToSymbol(this._profileService.getCurrencyDetails(balance.currency).symbol)} не задан`;
        return true;
      }

      return false;
    }).length === 0;
  }

  private isSelectedMonth(): boolean {
    const budget: Budget = this.data.budget;
    return budget.year === this.selectedYear && budget.month === this.selectedMonth;
  }
}
