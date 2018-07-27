import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { MatDialogModule, MatProgressSpinnerModule } from '@angular/material';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { PopoverModule } from 'ngx-popover';
import { LocalStorageModule } from 'angular-2-local-storage';
import { JwtModule } from '@auth0/angular-jwt';
import { MyDatePickerModule } from 'mydatepicker';

import { BookkeepingRootComponent } from './bk/bk.component';
import { BOOKKEEPING_ROUTES } from './routes';
import { AuthenticationComponent } from './authentication/authentication.component';
import { InputComponent } from './authentication/input/input.component';
import { ProfileService } from './common/service/profile.service';
import { LoadingService } from './common/service/loading.service';
import { environment } from '../environments/environment';
import { HOST } from './common/config/config';
import { HeaderComponent } from './bk/header/header.component';
import { HistoryComponent } from './history/history.component';
import { BudgetComponent } from './budget/budget.component';
import { SettingsComponent } from './settings/settings.component';
import { SummaryComponent } from './summary/summary.component';
import { SpinnerComponent } from './common/components/spinner/spinner.component';
import { CurrencyService } from './common/service/currency.service';
import { CurrencyConversionComponent } from './summary/currency-conversion/currency-conversion.component';
import { SummaryBodyComponent } from './summary/summary-body/summary-body.component';
import { SummaryFooterComponent } from './summary/summary-footer/summary-footer.component';
import { SummaryFooterPipe } from './common/pipes/summary/summary-footer.pipe';
import { CurrencySymbolComponent } from './common/components/currency-symbol/currency-symbol.component';
import { SummaryBalanceOrderingPipe } from './common/pipes/summary/summary-balance-ordering.pipe';
import { CurrencyValuePipe } from './common/pipes/currency-value.pipe';
import { SummaryBodyAccountPipe } from './common/pipes/summary/summary-body-account.pipe';
import { SummaryBodySubAccountPipe } from './common/pipes/summary/summary-body-subaccount.pipe';
import { HistoryService } from './common/service/history.service';
import { HistoryPageActionsComponent } from './history/history-page-actions/history-page-actions.component';
import { HistoryGroupPipe } from './common/pipes/history-group.pipe';
import { HistoryEditDialogComponent } from './history/history-edit-dialog/history-edit-dialog.component';
import { ConfirmDialogComponent } from './common/components/confirm-dialog/confirm-dialog.component';
import { ConfirmDialogService } from './common/components/confirm-dialog/confirm-dialog.service';
import { AlertService } from './common/service/alert.service';
import { GoalsContainerComponent } from './history/history-edit-dialog/goals-container/goals-container.component';
import { CurrencyValueDirective } from './common/directives/currency-value.directive';
import { SelectComponent } from './common/components/select/select.component';
import { FocusDirective } from './common/directives/focus.directive';
import { AssetImagePipe } from './common/pipes/asset-image.pipe';
import { LoadingDialogComponent } from './common/components/loading-dialog/loading-dialog.component';
import { MapKeysPipe } from './common/pipes/map-keys.pipe';
import { InputGroupComponent } from './history/history-edit-dialog/input-group/input-group.component';
import { AlternativeCurrenciesDialogComponent } from './history/history-edit-dialog/input-group/alternative-currencies-dialog/alternative-currencies-dialog.component';
import { BudgetService } from './common/service/budget.service';
import { GoalBudgetCategoryComponent } from './history/history-edit-dialog/goals-container/goal-budget-category/goal-budget-category.component';
import { TemplateVariableComponent } from './common/components/template-variable/template-variable.component';
import { GoalFilterPipe } from './common/pipes/goal-filter.pipe';
import { GoalSortPipe } from './common/pipes/goal-sort.pipe';
import { CurrenciesComponent } from './settings/currencies/currencies.component';
import { AccountsComponent } from './settings/accounts/accounts.component';
import { CategoriesComponent } from './settings/categories/categories.component';
import { ProfileComponent } from './settings/profile/profile.component';
import { CurrencySortPipe } from './common/pipes/currency-sort.pipe';
import { BalanceDialogComponent } from './settings/accounts/balance-dialog/balance-dialog.component';
import { AuthenticationService } from './common/service/authentication.service';
import { AuthenticationInterceptor } from './authentication.interceptor';
import { AccountCategoryDialogComponent } from './settings/account-category-dialog/account-category-dialog.component';
import { UsersComponent } from './users/users.component';
import { UserDialogComponent } from './users/user-dialog/user-dialog.component';
import { PopoverComponent } from './common/components/popover/popover.component';
import { BudgetDetailsComponent } from './budget/budget-details/budget-details.component';
import { MonthProgressComponent } from './budget/budget-details/month-progress/month-progress.component';
import { PlanBudgetDialogComponent } from './budget/plan-budget-dialog/plan-budget-dialog.component';
import { InputCalculatorComponent } from './common/components/input-calculator/input-calculator.component';

@NgModule({
  declarations: [
    BookkeepingRootComponent,
    AuthenticationComponent,
    InputComponent,
    HeaderComponent,
    HistoryComponent,
    BudgetComponent,
    SettingsComponent,
    SummaryComponent,
    SpinnerComponent,
    CurrencyConversionComponent,
    SummaryBodyComponent,
    SummaryFooterComponent,
    SummaryFooterPipe,
    CurrencySymbolComponent,
    SummaryBalanceOrderingPipe,
    CurrencyValuePipe,
    SummaryBodyAccountPipe,
    SummaryBodySubAccountPipe,
    HistoryPageActionsComponent,
    HistoryGroupPipe,
    HistoryEditDialogComponent,
    ConfirmDialogComponent,
    GoalsContainerComponent,
    CurrencyValueDirective,
    SelectComponent,
    FocusDirective,
    AssetImagePipe,
    LoadingDialogComponent,
    AlternativeCurrenciesDialogComponent,
    MapKeysPipe,
    InputGroupComponent,
    GoalBudgetCategoryComponent,
    TemplateVariableComponent,
    GoalFilterPipe,
    GoalSortPipe,
    CurrenciesComponent,
    AccountsComponent,
    CategoriesComponent,
    ProfileComponent,
    CurrencySortPipe,
    BalanceDialogComponent,
    AccountCategoryDialogComponent,
    UsersComponent,
    UserDialogComponent,
    PopoverComponent,
    BudgetDetailsComponent,
    MonthProgressComponent,
    PlanBudgetDialogComponent,
    InputCalculatorComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    JwtModule.forRoot({
      config: {
        tokenGetter: () => {return localStorage.getItem(`Bookkeeper.${AuthenticationService.TOKEN}`)},
        whitelistedDomains: ['localhost:8080', 'localhost:3000'],
        blacklistedRoutes: [/token\/generate-token.*/]
      }
    }),
    BrowserAnimationsModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    PopoverModule,
    MyDatePickerModule,
    RouterModule.forRoot(BOOKKEEPING_ROUTES),
    LocalStorageModule.withConfig({
      prefix: 'Bookkeeper',
      storageType: 'localStorage'
    })
  ],
  entryComponents: [
    ConfirmDialogComponent,
    HistoryEditDialogComponent,
    LoadingDialogComponent,
    AlternativeCurrenciesDialogComponent,
    AccountCategoryDialogComponent,
    BalanceDialogComponent,
    UserDialogComponent,
    PlanBudgetDialogComponent
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS,
      useClass: AuthenticationInterceptor,
      multi: true
    },
    CurrencyValuePipe,
    AssetImagePipe,
    ProfileService,
    LoadingService,
    CurrencyService,
    HistoryService,
    ConfirmDialogService,
    BudgetService,
    AlertService,
    {
      provide: HOST,
      useValue: environment.backendHost,
    }
  ],
  bootstrap: [BookkeepingRootComponent]
})
export class BookkeepingModule { }
