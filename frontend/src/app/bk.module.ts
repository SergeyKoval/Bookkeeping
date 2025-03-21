import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatCardModule } from '@angular/material/card';
import { DateAdapter, MAT_DATE_LOCALE } from '@angular/material/core';
import { MatMomentDateModule, MomentDateAdapter } from '@angular/material-moment-adapter';
import { MatTooltipModule } from '@angular/material/tooltip';

import { JwtModule } from '@auth0/angular-jwt';
import 'moment/locale/ru';

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
import { CurrencyService } from './common/service/currency.service';
import { CurrencyConversionComponent } from './common/components/currency-conversion/currency-conversion.component';
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
import { CurrencyDetailSortPipe } from './common/pipes/currency-detail-sort.pipe';
import { BalanceDialogComponent } from './settings/accounts/balance-dialog/balance-dialog.component';
import { AuthenticationService } from './common/service/authentication.service';
import { AuthenticationInterceptor } from './authentication.interceptor';
import { AccountCategoryDialogComponent } from './settings/account-category-dialog/account-category-dialog.component';
import { UsersComponent } from './users/users.component';
import { UserDialogComponent } from './users/user-dialog/user-dialog.component';
import { PopoverComponent } from './common/components/popover/popover.component';
import { BudgetDetailsComponent } from './budget/budget-details/budget-details.component';
import { MonthProgressComponent } from './common/components/month-progress/month-progress.component';
import { PlanBudgetDialogComponent } from './budget/plan-budget-dialog/plan-budget-dialog.component';
import { InputCalculatorComponent } from './common/components/input-calculator/input-calculator.component';
import { MoveGoalDialogComponent } from './budget/move-goal-dialog/move-goal-dialog.component';
import { MonthAndYearComponent } from './common/components/month-and-year/month-and-year.component';
import { CurrencySortPipe } from './common/pipes/currency-sort.pipe';
import { CategorySortPipe } from './common/pipes/category-sort.pipe';
import { DraggableDirective } from './common/directives/draggable.directive';
import { MoveSubCategoryDialogComponent } from './settings/categories/move-sub-category-dialog/move-sub-category-dialog.component';
import { CategoryStatisticsDialogComponent } from './budget/category-statistics-dialog/category-statistics-dialog.component';
import { CloseMonthDialogComponent } from './budget/close-month-dialog/close-month-dialog.component';
import { CloseMonthGoalFilterPipe } from './common/pipes/budget/close-month-goal-filter.pipe';
import { CloseMonthCategoryFilterPipe } from './common/pipes/budget/close-month-category-filter.pipe';
import { ReportActionsComponent } from './reports/report-actions/report-actions.component';
import { ReportSummaryComponent } from './reports/report-summary/report-summary.component';
import { ThreeStateCheckboxComponent } from './common/components/three-state-checkbox/three-state-checkbox.component';
import { StopPropagationDirective } from './common/directives/stop-propagation.directive';
import { MultiLevelDropdownComponent } from './common/components/multi-level-dropdown/multi-level-dropdown.component';
import { MultiLevelDropdownLevelComponent } from './common/components/multi-level-dropdown/multi-level-dropdown-level/multi-level-dropdown-level.component';
import { ReportDynamicComponent } from './reports/report-dynamic/report-dynamic.component';
import { DevicesComponent } from './settings/devices/devices.component';
import { DeviceMailDialogComponent } from './settings/devices/device-mail-dialog/device-mail-dialog.component';
import { DeviceNameDialogComponent } from './settings/devices/device-name-dialog/device-name-dialog.component';
import { DeviceMessageDialogComponent } from './settings/devices/device-message-dialog/device-message-dialog.component';
import { DeviceMessageAssignDialogComponent } from './history/device-message-assign-dialog/device-message-assign-dialog.component';
import { ToggleComponent } from './common/components/toggle/toggle.component';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { SpinnerComponent } from './common/components/spinner/spinner.component';
import { NgChartsModule } from 'ng2-charts';
import { PopoverModule } from 'ngx-bootstrap/popover';
import { PeriodFilterComponent } from './reports/period-filter/period-filter.component';

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
    CurrencyDetailSortPipe,
    BalanceDialogComponent,
    AccountCategoryDialogComponent,
    UsersComponent,
    UserDialogComponent,
    PopoverComponent,
    BudgetDetailsComponent,
    MonthProgressComponent,
    PlanBudgetDialogComponent,
    InputCalculatorComponent,
    MoveGoalDialogComponent,
    MonthAndYearComponent,
    CurrencySortPipe,
    CategorySortPipe,
    DraggableDirective,
    MoveSubCategoryDialogComponent,
    CategoryStatisticsDialogComponent,
    CloseMonthDialogComponent,
    CloseMonthGoalFilterPipe,
    CloseMonthCategoryFilterPipe,
    ReportActionsComponent,
    ReportSummaryComponent,
    ThreeStateCheckboxComponent,
    StopPropagationDirective,
    MultiLevelDropdownComponent,
    MultiLevelDropdownLevelComponent,
    ReportDynamicComponent,
    DevicesComponent,
    DeviceMailDialogComponent,
    DeviceNameDialogComponent,
    DeviceMessageDialogComponent,
    DeviceMessageAssignDialogComponent,
    ToggleComponent,
    InputComponent,
    SpinnerComponent
  ],
  bootstrap: [BookkeepingRootComponent],
  imports: [
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    JwtModule.forRoot({
      config: {
        tokenGetter: () => localStorage.getItem(AuthenticationService.TOKEN),
        allowedDomains: ['localhost:8085', 'localhost:3000'],
        disallowedRoutes: [/token\/server\/version.*/, /token\/generate-token.*/, /token\/send-registration-code.*/, /token\/review-registration-code.*/]
      }
    }),
    BrowserAnimationsModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    MatSlideToggleModule,
    PopoverModule,
    NgChartsModule,
    RouterModule.forRoot(BOOKKEEPING_ROUTES),
    MatDatepickerModule,
    MatMomentDateModule,
    MatCardModule,
    MatTooltipModule,
    PeriodFilterComponent
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
    },
    { provide: MAT_DATE_LOCALE, useValue: 'ru-RU' },
    { provide: DateAdapter, useClass: MomentDateAdapter, deps: [MAT_DATE_LOCALE] },
    provideHttpClient(withInterceptorsFromDi())
  ] })
export class BookkeepingModule { }
