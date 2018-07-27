import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material';
import { CurrencyUtils } from '../../common/utils/currency-utils';

@Component({
  selector: 'bk-plan-budget-dialog',
  templateUrl: './plan-budget-dialog.component.html',
  styleUrls: ['./plan-budget-dialog.component.css']
})
export class PlanBudgetDialogComponent implements OnInit {
  public constructor(
    @Inject(MAT_DIALOG_DATA) public data: {editMode: boolean, type: string},
  ) {}

  public ngOnInit(): void {
  }
}
