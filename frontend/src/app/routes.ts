import {Route} from '@angular/router';

import {AuthenticationComponent} from './authentication/authentication.component';
import {BookkeepingComponent} from './bookkeeping/bookkeeping.component';
import {BudgetComponent} from './bookkeeping/budget/budget.component';
import {HistoryComponent} from './bookkeeping/history/history.component';
import {SettingsComponent} from './bookkeeping/settings/settings.component';
import {AuthenticationService} from './common/service/authentication.service';

export const BOOKKEEPING_ROUTES: Route[] = [
  {
    path: '',
    redirectTo: 'authentication',
    pathMatch: 'full'
  },
  {
    path: 'authentication',
    component: AuthenticationComponent
  },
  {
    path: 'bookkeeping',
    component: BookkeepingComponent,
    canActivate: [AuthenticationService],
    children: [
      {
        path: '',
        redirectTo: 'budget',
        pathMatch: 'full'
      },
      {
        path: 'budget',
        component: BudgetComponent
      },
      {
        path: 'history',
        component: HistoryComponent
      },
      {
        path: 'settings',
        component: SettingsComponent
      }
    ]
  }
];
