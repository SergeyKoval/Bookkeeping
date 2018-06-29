import { Injectable } from '@angular/core';
import { MatDialogRef } from '@angular/material';

import { Subject } from 'rxjs';

import { LoadingDialogComponent } from '../components/loading-dialog/loading-dialog.component';
import { DialogService } from './dialog.service';

@Injectable()
export class LoadingService {
  private _authenticationCheck$$: Subject<boolean> = new Subject();
  private _accounts$$: Subject<boolean> = new Subject();
  private _categories$$: Subject<boolean> = new Subject();

  public constructor(private _dialogService: DialogService) {}

  public openLoadingDialog(title: string): MatDialogRef<LoadingDialogComponent> {
    return this._dialogService.openDialog(LoadingDialogComponent, {
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
