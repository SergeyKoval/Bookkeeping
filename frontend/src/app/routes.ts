import { Route } from '@angular/router';

import { AuthenticationComponent } from './authentication/authentication.component';
import { BudgetComponent } from './budget/budget.component';
import { HistoryComponent } from './history/history.component';
import { SettingsComponent } from './settings/settings.component';
import { CurrenciesComponent } from './settings/currencies/currencies.component';
import { AccountsComponent } from './settings/accounts/accounts.component';
import { CategoriesComponent } from './settings/categories/categories.component';
import { ProfileComponent } from './settings/profile/profile.component';
import { AuthenticationService } from './common/service/authentication.service';
import { UsersComponent } from './users/users.component';
import { ProfileService } from './common/service/profile.service';

export const BOOKKEEPING_ROUTES: Route[] = [
  {
    path: '',
    redirectTo: 'budget',
    pathMatch: 'full'
  },
  {
    path: 'authentication',
    component: AuthenticationComponent
  },
  {
    path: 'budget',
    component: BudgetComponent,
    canActivate: [AuthenticationService]
  },
  {
    path: 'history',
    component: HistoryComponent,
    canActivate: [AuthenticationService]
  },
  {
    path: 'users',
    component: UsersComponent,
    canActivate: [AuthenticationService, ProfileService],
  },
  {
    path: 'settings',
    component: SettingsComponent,
    canActivate: [AuthenticationService],
    children: [
      {
        path: '',
        redirectTo: 'profile',
        pathMatch: 'full'
      },
      {
        path: 'profile',
        component: ProfileComponent
      },
      {
        path: 'currencies',
        component: CurrenciesComponent
      },
      {
        path: 'accounts',
        component: AccountsComponent
      },
      {
        path: 'categories',
        component: CategoriesComponent
      }
    ]
  }
];
