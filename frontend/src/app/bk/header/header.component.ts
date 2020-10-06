import { ChangeDetectionStrategy, Component } from '@angular/core';

import { Observable } from 'rxjs';

import { AuthenticationService } from '../../common/service/authentication.service';
import { AdminGuard } from '../../common/guards/admin.guard';

@Component({
  selector: 'bk-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HeaderComponent {

  public constructor(
    private _authenticationService: AuthenticationService,
    private _adminGuard: AdminGuard
  ) {}

  public isAdmin(): Observable<boolean> {
    return this._adminGuard.canActivate(null, null);
  }

  public exit(): void {
    this._authenticationService.exit();
  }
}
