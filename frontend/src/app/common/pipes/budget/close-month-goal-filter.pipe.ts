import { Pipe, PipeTransform } from '@angular/core';

import { GoalWrapper } from '../../model/budget/GoalWrapper';
import { CloseMonthFilter } from '../../model/budget/CloseMonthFilter';

@Pipe({
    name: 'closeMonthGoalFilter',
    standalone: false
})
export class CloseMonthGoalFilterPipe implements PipeTransform {
  public transform(goals: GoalWrapper[], filters: CloseMonthFilter[]): GoalWrapper[] {
    if (!filters || filters.length === 0) {
      return goals;
    }

    filters.forEach(filter => goals = CloseMonthGoalFilterPipe.filter(goals, filter));
    return goals;
  }

  private static filter(goals: GoalWrapper[], filter: CloseMonthFilter): GoalWrapper[] {
    switch (filter) {
      case CloseMonthFilter.DONE:
        return goals.filter(goalWrapper => goalWrapper.goal.done);
      case CloseMonthFilter.UNDONE:
        return goals.filter(goalWrapper => !goalWrapper.goal.done);
      case CloseMonthFilter.USED:
        return goals.filter(goalWrapper => goalWrapper.actionPlan);
      case CloseMonthFilter.UNUSED:
        return goals.filter(goalWrapper => !goalWrapper.actionPlan);
      case CloseMonthFilter.INCOME:
        return goals.filter(goalWrapper => goalWrapper.type === 'income');
      case CloseMonthFilter.EXPENSE:
        return goals.filter(goalWrapper => goalWrapper.type === 'expense');
    }
  }

}
