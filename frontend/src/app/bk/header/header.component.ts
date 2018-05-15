import { Component } from '@angular/core';

import { AuthenticationService } from '../../common/service/authentication.service';

@Component({
  selector: 'bk-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent {

  public constructor(private _authenticationService: AuthenticationService) {}

  public exit(): void {
    this._authenticationService.exit();
  }
}
