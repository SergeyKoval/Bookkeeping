import { Injectable } from '@angular/core';
import { MatDialogRef } from '@angular/material';
import { MatDialog } from '@angular/material/dialog';

import { Subject } from 'rxjs';

import { LoadingDialogComponent } from '../components/loading-dialog/loading-dialog.component';

@Injectable()
export class LoadingService {
  private _authenticationCheck$$: Subject<boolean> = new Subject();
  private _accounts$$: Subject<boolean> = new Subject();
  private _categories$$: Subject<boolean> = new Subject();

  public constructor(private _dialog: MatDialog) {}

  public openLoadingDialog(title: string): MatDialogRef<LoadingDialogComponent> {
    return this._dialog.open(LoadingDialogComponent, {
      disableClose: true,
      data: {'title': title},
    });
  }

  public get authenticationCheck$$(): Subject<boolean> {
    return this._authenticationCheck$$;
  }

  public get accounts$$(): Subject<boolean> {
    return this._accounts$$;
  }

  public get categories$$(): Subject<boolean> {
    return this._categories$$;
  }
}
