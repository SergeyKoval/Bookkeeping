import { GoalWrapper } from './GoalWrapper';

export class CategoryWrapper {
  public constructor(
    public category: BudgetCategory,
    public type: string,
    public goalWrappers: GoalWrapper[],
    public removable: boolean = true,
    public missedInClosedMonth: boolean = false,
    public actionPlan: {[currency: string]: BudgetBalance} = null
  ) {}
}
