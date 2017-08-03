import { Injectable } from '@angular/core';

import {Subject} from 'rxjs/Subject';

@Injectable()
export class LoadingService {
  private _authentication$$: Subject<boolean> = new Subject();
  private _accounts$$: Subject<boolean> = new Subject();
  private _categories$$: Subject<boolean> = new Subject();

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
