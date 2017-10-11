import { Pipe, PipeTransform } from '@angular/core';

import { HistoryService } from '../service/history.service';

@Pipe({
  name: 'goalSort'
})
export class GoalSortPipe implements PipeTransform {
  public transform(goals: BudgetGoal[], selectedGoal?: BudgetGoal): BudgetGoal[] {
    return HistoryService.sortGoals(goals, selectedGoal);
  }
}
