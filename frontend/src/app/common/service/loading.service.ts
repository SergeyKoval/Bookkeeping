import { Injectable } from '@angular/core';
import {MdDialog, MdDialogRef} from '@angular/material';

import {Subject} from 'rxjs/Subject';

import {LoadingDialogComponent} from '../components/loading-dialog/loading-dialog.component';

@Injectable()
export class LoadingService {
  private _authentication$$: Subject<boolean> = new Subject();
  private _accounts$$: Subject<boolean> = new Subject();
  private _categories$$: Subject<boolean> = new Subject();

  public constructor(private _dialog: MdDialog) {}

  public openLoadingDialog(title: string): MdDialogRef<LoadingDialogComponent> {
    return this._dialog.open(LoadingDialogComponent, {
      disableClose: true,
      data: {'title': title},
    });
  }

  public get authentication$$(): Subject<boolean> {
    return this._authentication$$;
  }

  public get accounts$$(): Subject<boolean> {
    return this._accounts$$;
  }

  public get categories$$(): Subject<boolean> {
    return this._categories$$;
  }
}
