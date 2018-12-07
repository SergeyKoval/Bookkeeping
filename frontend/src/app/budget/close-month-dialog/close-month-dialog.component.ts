import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material';

@Component({
  selector: 'bk-close-month-dialog',
  templateUrl: './close-month-dialog.component.html',
  styleUrls: ['./close-month-dialog.component.css']
})
export class CloseMonthDialogComponent implements OnInit {
  public type: string = 'goals';

  public constructor(
    private _dialogRef: MatDialogRef<CloseMonthDialogComponent>
  ) {}

  public ngOnInit(): void {
  }

  public onChangeSelectedType(selectedType: string): void {
    this.type = selectedType;
  }

  public close(): void {
    this._dialogRef.close();
  }

}
