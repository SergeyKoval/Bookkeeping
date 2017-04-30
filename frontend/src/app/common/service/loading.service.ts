import { Injectable } from '@angular/core';

import {Subject} from 'rxjs/Subject';

@Injectable()
export class LoadingService {
  private _authentication$$: Subject<boolean> = new Subject();
  private _summary$$: Subject<boolean> = new Subject();

  public get authentication$$(): Subject<boolean> {
    return this._authentication$$;
  }

  public get summary$$(): Subject<boolean> {
    return this._summary$$;
  }
}
