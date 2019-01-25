import { Component } from '@angular/core';

import { Observable } from 'rxjs';

import { AuthenticationService } from '../../common/service/authentication.service';
import { ProfileService } from '../../common/service/profile.service';

@Component({
  selector: 'bk-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent {

  public constructor(
    private _authenticationService: AuthenticationService,
    private _profileService: ProfileService
  ) {}

  public isAdmin(): Observable<boolean> {
    return this._profileService.canActivate(null, null);
  }

  public exit(): void {
    this._authenticationService.exit();
  }

  public needDisplayReports(): boolean {
    return this._profileService.authenticatedProfile.email === 'skoval@gmail.com';
  }
}
