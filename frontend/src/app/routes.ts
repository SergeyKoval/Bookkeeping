import { Route } from '@angular/router';

import { AuthenticationComponent } from './authentication/authentication.component';
import { BookkeepingComponent } from './bookkeeping/bookkeeping.component';
import { BudgetComponent } from './bookkeeping/budget/budget.component';
import { HistoryComponent } from './bookkeeping/history/history.component';
import { SettingsComponent } from './bookkeeping/settings/settings.component';
import { ProfileService } from './common/service/profile.service';
import { CurrenciesComponent } from './bookkeeping/settings/currencies/currencies.component';
import { AccountsComponent } from './bookkeeping/settings/accounts/accounts.component';
import { CategoriesComponent } from './bookkeeping/settings/categories/categories.component';

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
    canActivate: [ProfileService],
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
        component: SettingsComponent,
        children: [
          {
            path: '',
            redirectTo: 'currencies',
            pathMatch: 'full'
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
    ]
  }
];
