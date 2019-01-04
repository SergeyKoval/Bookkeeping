export class GoalWrapper {
  public actionPlan: CloseMonthGoalPlan;

  public constructor(
    public goal: BudgetGoal,
    public type: string,
    public category: string,
    public removable: boolean = true,
    public missedInClosedMonth: boolean = false
  ) {}
}
