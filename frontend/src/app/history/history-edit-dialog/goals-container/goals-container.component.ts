import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

import { Observable, of } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Moment } from 'moment/moment';

import { BudgetService } from '../../../common/service/budget.service';
import { GoalFilterType } from '../../../common/model/history/GoalFilterType';
import { DateUtils } from '../../../common/utils/date-utils';
import { ConfirmDialogService } from '../../../common/components/confirm-dialog/confirm-dialog.service';
import { CurrencyUtils } from '../../../common/utils/currency-utils';
import { ProfileService } from '../../../common/service/profile.service';
import { HistoryType } from '../../../common/model/history/history-type';
import { GoalDetails } from '../../../common/model/budget/goal-details';
import { MonthProgress } from '../../../common/model/budget/month-progress';
import { BudgetCategory } from '../../../common/model/budget/budget-category';
import { BudgetGoal } from '../../../common/model/budget/budget-goal';
import { Budget } from '../../../common/model/budget/budget';
import { DeviceMessage } from '../../../common/model/history/deviceMessage';
import { BudgetBalance } from '../../../common/model/budget/budget-balance';
import { HistoryBalanceType } from '../../../common/model/history/history-balance-type';

@Component({
    selector: 'bk-goals-container',
    templateUrl: './goals-container.component.html',
    styleUrls: ['./goals-container.component.css'],
    standalone: false
})
export class GoalsContainerComponent implements OnInit {
  public FILTER_TYPES: GoalFilterType[] = [GoalFilterType.NOT_DONE, GoalFilterType.DONE, GoalFilterType.ALL];

  @Input()
  public historyItem: HistoryType;
  @Input()
  public editMode: boolean;
  @Input()
  public alternativeCurrencyLoading: boolean;
  @Input()
  public fromDeviceMessage: boolean;
  @Input()
  public set selectedCategory(value: string) {
    if (this._selectedCategory !== value && this._budget) {
      Promise.resolve().then(() => {
        this.changePreviouslySelectedGoal().subscribe(() => {
          this.selectedGoal = null;
          this.historyItem.goal = null;
          this._selectedCategory = value;
          this.chooseBudgetCategory(this._budget);
        });
      });
    } else {
      this._selectedCategory = value;
    }
  }
  @Input()
  public set selectedDate(value: Moment) {
    const dateChanged: boolean = this._selectedDate && this._selectedDate.format('YYYYMM') !== value.format('YYYYMM');

    if (dateChanged) {
      this.updateMonthProgress(value);
      this.loadBudget(value);

      Promise.resolve().then(() => {
        this.changePreviouslySelectedGoal().subscribe(() => {
          this.selectedGoal = null;
          this.historyItem.goal = null;
          this._selectedDate = value;
        });
      });
    } else {
      this._selectedDate = value;
    }
  }
  @Input()
  public set goalCouldBeCalculated(value: boolean) {
    if (value && !this._goalCouldBeCalculated) {
      this._originalHistoryItem = Object.assign({}, this.historyItem);
      this._originalHistoryItem.balance = Object.assign({}, this.historyItem.balance);
      this.goalFilterType = this.editMode ? GoalFilterType.ALL : GoalFilterType.NOT_DONE;
      this.updateMonthProgress(this._selectedDate);
      this.loadBudget(this._selectedDate);
    }
    this._goalCouldBeCalculated = value;
  }

  @Output()
  public originalGoalDetailsChange: EventEmitter<GoalDetails> = new EventEmitter();
  @Output()
  public goalDetailsChange: EventEmitter<GoalDetails> = new EventEmitter();

  public goalFilterType: GoalFilterType;
  public budgetLoading: boolean;
  public monthProgress: MonthProgress;
  public budgetCategory: BudgetCategory;
  public selectedGoal: BudgetGoal;
  public deviceMessageTabSelected: boolean;
  public selectedDeviceMessageIndex: number;

  private _budget: Budget;
  private _selectedCategory: string;
  private _selectedDate: Moment;
  private _originalHistoryItem: HistoryType;
  private _originalBudget: Budget;
  private _originalCategory: BudgetCategory;
  private _originalGoalDetails: GoalDetails;
  private _goalDetails: GoalDetails;
  private _goalCouldBeCalculated: boolean;

  public constructor(
    private _budgetService: BudgetService,
    private _confirmDialogService: ConfirmDialogService,
    private _profileService: ProfileService
  ) { }

  public ngOnInit(): void {
    this.deviceMessageTabSelected = this.historyItem.deviceMessages && this.historyItem.deviceMessages.length > 0;
    this.selectedDeviceMessageIndex = this.deviceMessageTabSelected ? 0 : null;
  }

  public getGoalCount(filterType: GoalFilterType): number {
    return this.budgetCategory && this.budgetCategory.goals ? BudgetService.filterGoals(this.budgetCategory.goals, filterType).length : 0;
  }

  public getPercentBeforeSelection(goalItem: BudgetGoal): number {
    if (goalItem.done === true && (!this.isSelectedGoal(goalItem) || goalItem.balance.value >= goalItem.balance.completeValue)) {
      return 100;
    }

    const percent: number = Math.round(goalItem.balance.value / goalItem.balance.completeValue * 100);
    return percent < 100 ? percent : 100;
  }

  public getSelectedPercent(goalItem: BudgetGoal): number {
    const previousPercent: number = this.getPercentBeforeSelection(goalItem);
    const selectedGoalAffectedValue: number = this.convertValueToCurrency(this.historyItem.balance, goalItem.balance.currency);
    const selectedPercent: number = Math.round(selectedGoalAffectedValue / this.selectedGoal.balance.completeValue * 100);
    return (selectedPercent + previousPercent) < 100 ? selectedPercent : (100 - previousPercent);
  }

  public getGoalValue(goalItem: BudgetGoal): number {
    return goalItem !== this.selectedGoal ? goalItem.balance.value : (goalItem.balance.value + this.convertValueToCurrency(this.historyItem.balance, goalItem.balance.currency));
  }

  public getGoalPercent(goalItem: BudgetGoal): number {
    const goalPercent: number = this.getGoalValue(goalItem) / goalItem.balance.completeValue * 100;
    if (this._goalDetails && this.isSelectedGoal(goalItem)) {
      this._goalDetails.percent = goalPercent;
    }
    return goalPercent;
  }

  public chooseGoal(goalItem: BudgetGoal): void {
    if (goalItem.title !== this.historyItem.goal) {
      this.revertSelectedGoalOriginalStatus();

      if (this.editMode && this._originalGoalDetails
        && this._originalGoalDetails.done === true
        && this.historyItem.year === this._originalHistoryItem.year
        && this.historyItem.month === this._originalHistoryItem.month
        && this.historyItem.category === this._originalHistoryItem.category
        && goalItem.title === this._originalHistoryItem.goal
      ) {
        goalItem.done = true;
        this._originalGoalDetails.changeStatus = false;
      }

      this.changePreviouslySelectedGoal().subscribe(() => {
        this.selectedGoal = goalItem;
        this.historyItem.goal = goalItem.title;
        this._goalDetails = {
          changeStatus: false,
          done: this.selectedGoal.done,
          title: this.selectedGoal.title,
          currency: this.selectedGoal.balance.currency,
          value: this.selectedGoal.balance.value,
          completeValue: this.selectedGoal.balance.completeValue,
          percent: this.selectedGoal.balance.value / this.selectedGoal.balance.completeValue * 100
        };
        this.goalDetailsChange.next(this._goalDetails);
      });
    }
  }

  public uncheckGoal(): void {
    this.revertSelectedGoalOriginalStatus();
    this.changePreviouslySelectedGoal().subscribe(() => {
      this.selectedGoal = null;
      this.historyItem.goal = null;
    });
  }

  public changeGoalStatus(goalItem: BudgetGoal): void {
    if (goalItem.title === this.historyItem.goal) {
      this.selectedGoal.done = !this.selectedGoal.done;
      this._goalDetails.changeStatus = !this._goalDetails.changeStatus;
    }
  }

  public getGoalDonePopoverTitle(goal: BudgetGoal): string {
    return goal.done
      ? this.isSelectedGoal(goal) ? 'Пометить невыполненным' : 'Выполнено'
      : this.isSelectedGoal(goal) ? 'Пометить выполненным' : 'Не выполнено';
  }

  public isSelectedGoal(goal: BudgetGoal): boolean {
    return goal.title === this.historyItem.goal;
  }

  public getDeviceMessageTimestamp(deviceMessage: DeviceMessage): string {
    const date: Date = new Date(deviceMessage.messageTimestamp);
    return `${date.toLocaleDateString('ru-RU')} ${date.toLocaleTimeString('ru-RU')}`;
  }

  public getFormattedDeviceMessage(): string {
    return this.historyItem.deviceMessages[this.selectedDeviceMessageIndex].fullText.split('\n').join('<br>');
  }

  public get selectedCategory(): string {
    return this._selectedCategory;
  }

  public get selectedDate(): Moment {
    return this._selectedDate;
  }

  public get goalCouldBeCalculated(): boolean {
    return this._goalCouldBeCalculated;
  }

  private loadBudget(date: Moment): void {
    this.budgetLoading = true;
    this._budgetService.loadBudget(date.year(), date.month() + 1).subscribe((budget: Budget) => {
      this._budget = budget;
      this.chooseBudgetCategory(budget);

      if (this.editMode && !this.fromDeviceMessage) {
        if (this.historyItem.goal && !this._originalGoalDetails) {
          this.populateOriginalGoal(budget);
        }
        if (budget.year === this._originalHistoryItem.year && budget.month === this._originalHistoryItem.month) {
          const category: BudgetCategory = budget[this.historyItem.type].categories.filter(budgetCategory => budgetCategory.title === this._originalHistoryItem.category)[0];
          const categoryBalance: {[currency: string]: BudgetBalance} = category.balance;
          const originalHistoryCurrency: string = this._originalHistoryItem.balance.currency;
          categoryBalance[originalHistoryCurrency].value = categoryBalance[originalHistoryCurrency].value - this._originalHistoryItem.balance.value;

          if (this._originalHistoryItem.goal) {
            const budgetGoal: BudgetGoal = category.goals.filter(goal => goal.title === this._originalHistoryItem.goal)[0];
            budgetGoal.balance.value = this._originalGoalDetails.value;
            if (this._originalGoalDetails.changeStatus === true) {
              budgetGoal.done = !this._originalGoalDetails.done;
            }
            this.originalGoalDetailsChange.next(this._originalGoalDetails);
          }
        }
      }

      this.budgetLoading = false;
    });
  }

  private updateMonthProgress(date: Moment): void {
    const now: Date = new Date();
    const currentMonth: boolean = now.getFullYear() === date.year() && now.getMonth() === date.month();
    const monthPercent: number = currentMonth ? Math.round(now.getDate() / DateUtils.daysInMonth(date.year(), date.month() + 1) * 100) : 0;
    this.monthProgress = {'currentMonth': currentMonth, 'monthPercent': monthPercent};
  }

  private populateOriginalGoal(budget: Budget): void {
    this._originalBudget = budget;
    this._originalCategory = budget[this.historyItem.type].categories.filter(category => category.title === this.selectedCategory)[0];
    this.selectedGoal = this._originalCategory.goals.filter(goal => goal.title === this.historyItem.goal)[0];

    const goalBalance: BudgetBalance = this.selectedGoal.balance;
    const goalCurrency: string = goalBalance.currency;
    const originalBalance: HistoryBalanceType = this._originalHistoryItem.balance;
    const goalValue: number = goalCurrency === originalBalance.currency ? originalBalance.value : this.convertValueToCurrency(originalBalance, goalCurrency);
    this._originalGoalDetails = {
      done: this.selectedGoal.done,
      title: this.selectedGoal.title,
      currency: goalBalance.currency,
      value: goalBalance.value - goalValue,
      completeValue: goalBalance.completeValue,
      changeStatus: false,
      percent: (goalBalance.value - goalValue) / goalBalance.completeValue * 100
    };
    this._goalDetails = Object.assign({}, this._originalGoalDetails);
    this.originalGoalDetailsChange.next(this._originalGoalDetails);
    this.goalDetailsChange.next(this._goalDetails);
  }

  private convertValueToCurrency(balance: HistoryBalanceType, resultCurrency: string): number {
      if (balance.currency === resultCurrency || !balance.value) {
      return balance.value || 0;
    }

    return balance.alternativeCurrency[resultCurrency];
  }

  private chooseBudgetCategory(budget: Budget): void {
    this.budgetCategory = budget[this.historyItem.type].categories.filter(category => category.title === this._selectedCategory)[0];
    if (this.budgetCategory && this.budgetCategory.goals) {
      this.budgetCategory.goals = BudgetService.sortGoals(this.budgetCategory.goals, this.selectedGoal);
    }
  }

  private revertSelectedGoalOriginalStatus(): void {
    if (this.selectedGoal && this._goalDetails.changeStatus === true) {
      this.selectedGoal.done = !this.selectedGoal.done;
      this._goalDetails.changeStatus = false;
    }
  }

  private changePreviouslySelectedGoal(): Observable<Object> {
    if (this.editMode
      && this._originalGoalDetails
      && this._originalGoalDetails.done === true
      && this._selectedDate.year() === this._originalHistoryItem.year
      && this._selectedDate.month() + 1 === this._originalHistoryItem.month
      && this.historyItem.category === this._originalHistoryItem.category
      && this.historyItem.goal === this._originalHistoryItem.goal
      && this._originalGoalDetails.value < this._originalGoalDetails.completeValue
    ) {
      return this._confirmDialogService.openConfirmDialogWithHtml('Изменение статуса цели',
        `<div>Операция является частью цели <strong>${this._originalHistoryItem.goal}</strong>.</div>
                  <div>
                    После изменений цель будет выплнена
                    <strong>
                      ${CurrencyUtils.convertCodeToSymbol(this._profileService.getCurrencyDetails(this._originalGoalDetails.currency).symbol)}
                      ${this._originalGoalDetails.value} / ${this._originalGoalDetails.completeValue}
                    </strong>
                  </div>
                  <div>Изменить статус цели на невыполненную?</div>`
      ).afterClosed()
        .pipe(tap(result => {
          if (result === true) {
            this.selectedGoal.done = false;
            this._goalDetails.changeStatus = false;
          }

          this._originalGoalDetails.changeStatus = result;
        }));
    } else {
      return of({});
    }
  }
}
