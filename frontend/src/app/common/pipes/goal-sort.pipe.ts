import { Pipe, PipeTransform } from '@angular/core';

import { BudgetService } from '../service/budget.service';
import { BudgetGoal } from '../model/budget/budget-goal';

@Pipe({
  name: 'goalSort'
})
export class GoalSortPipe implements PipeTransform {
  public transform(goals: BudgetGoal[], selectedGoal?: BudgetGoal): BudgetGoal[] {
    return BudgetService.sortGoals(goals, selectedGoal);
  }
}
