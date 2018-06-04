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
    component: AuthenticationComponent,
    data: {title: 'Бухгалтерия - аутентификация'}
  },
  {
    path: 'budget',
    component: BudgetComponent,
    canActivate: [AuthenticationService],
    data: {title: 'Бухгалтерия - бюджет'}
  },
  {
    path: 'history',
    component: HistoryComponent,
    canActivate: [AuthenticationService],
    data: {title: 'Бухгалтерия - учет'}
  },
  {
    path: 'users',
    component: UsersComponent,
    canActivate: [AuthenticationService, ProfileService],
    data: {title: 'Бухгалтерия - пользователи'}
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
        component: ProfileComponent,
        data: {title: 'Бухгалтерия - настройки - профиль'}
      },
      {
        path: 'currencies',
        component: CurrenciesComponent,
        data: {title: 'Бухгалтерия - настройки - валюты'}
      },
      {
        path: 'accounts',
        component: AccountsComponent,
        data: {title: 'Бухгалтерия - настройки - счета'}
      },
      {
        path: 'categories',
        component: CategoriesComponent,
        data: {title: 'Бухгалтерия - настройки - категории'}
      }
    ]
  }
];
