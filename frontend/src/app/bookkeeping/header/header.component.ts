import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { ProfileService } from '../../common/service/profile.service';

@Component({
  selector: 'bk-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent {

  public constructor(
    private _authenticationService: ProfileService,
    private _router: Router
  ) {}

  public exit(): void {
    this._authenticationService.exit();
    this._router.navigate(['/authentication']);
  }
}
