import { Pipe, PipeTransform } from '@angular/core';

import { GoalFilterType } from '../model/history/GoalFilterType';
import { HistoryService } from '../service/history.service';

@Pipe({
  name: 'goalFilter'
})
export class GoalFilterPipe implements PipeTransform {
  public transform(goals: BudgetGoal[], filter: GoalFilterType): BudgetGoal[] {
    return HistoryService.filterGoals(goals, filter);
  }
}
