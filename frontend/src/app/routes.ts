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
import { ReportActionsComponent } from './reports/report-actions/report-actions.component';
import { ReportSummaryComponent } from './reports/report-summary/report-summary.component';
import { ReportDynamicComponent } from './reports/report-dynamic/report-dynamic.component';
import { DevicesComponent } from './settings/devices/devices.component';
import { TagsComponent } from './settings/tags/tags.component';

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
    path: 'reports',
    canActivate: [AuthenticationService],
    children: [
      {
        path: '',
        redirectTo: 'actions',
        pathMatch: 'full'
      },
      {
        path: 'actions',
        component: ReportActionsComponent,
        data: {title: 'Бухгалтерия - отчеты - операции за период'}
      },
      {
        path: 'summary',
        component: ReportSummaryComponent,
        data: {title: 'Бухгалтерия - отчеты - итоги за период'}
      },
      {
        path: 'dynamic',
        component: ReportDynamicComponent,
        data: {title: 'Бухгалтерия - отчеты - динамика за период'}
      }
    ]
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
      },
      {
        path: 'devices',
        component: DevicesComponent,
        data: {title: 'Бухгалтерия - настройки - девайсы'}
      },
      {
        path: 'tags',
        component: TagsComponent,
        data: {title: 'Бухгалтерия - настройки - тэги'}
      }
    ]
  }
];
