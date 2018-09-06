import { Pipe, PipeTransform } from '@angular/core';

import { GoalFilterType } from '../model/history/GoalFilterType';
import { BudgetService } from '../service/budget.service';

@Pipe({
  name: 'goalFilter'
})
export class GoalFilterPipe implements PipeTransform {
  public transform(goals: BudgetGoal[], filter: GoalFilterType): BudgetGoal[] {
    return BudgetService.filterGoals(goals, filter);
  }
}
