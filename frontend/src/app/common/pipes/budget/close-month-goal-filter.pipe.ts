import { Pipe, PipeTransform } from '@angular/core';

import { GoalWrapper } from '../../model/budget/GoalWrapper';
import { GoalFilter } from '../../model/budget/GoalFilter';

@Pipe({
  name: 'closeMonthGoalFilter'
})
export class CloseMonthGoalFilterPipe implements PipeTransform {
  public transform(goals: GoalWrapper[], filters: GoalFilter[]): GoalWrapper[] {
    if (!filters || filters.length === 0) {
      return goals;
    }

    filters.forEach(filter => goals = CloseMonthGoalFilterPipe.filter(goals, filter));
    return goals;
  }

  private static filter(goals: GoalWrapper[], filter: GoalFilter): GoalWrapper[] {
    switch (filter) {
      case GoalFilter.DONE:
        return goals.filter(goalWrapper => goalWrapper.goal.done);
      case GoalFilter.UNDONE:
        return goals.filter(goalWrapper => !goalWrapper.goal.done);
      case GoalFilter.USED:
        return goals.filter(goalWrapper => goalWrapper.actionPlan);
      case GoalFilter.UNUSED:
        return goals.filter(goalWrapper => !goalWrapper.actionPlan);
      case GoalFilter.INCOME:
        return goals.filter(goalWrapper => goalWrapper.type === 'income');
      case GoalFilter.EXPENSE:
        return goals.filter(goalWrapper => goalWrapper.type === 'expense');
    }
  }

}
