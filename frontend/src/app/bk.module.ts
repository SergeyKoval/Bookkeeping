import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { RouterModule } from '@angular/router';
import { MdDialogModule, MdProgressSpinnerModule } from '@angular/material';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { PopoverModule } from 'ngx-popover';

import { BookkeepingRootComponent } from './bk.component';
import { BOOKKEEPING_ROUTES } from './routes';
import { AuthenticationComponent } from './authentication/authentication.component';
import { InputComponent } from './authentication/input/input.component';
import { AuthenticationService } from './common/service/authentication.service';
import { LoadingService } from './common/service/loading.service';
import { environment } from '../environments/environment';
import { HOST } from './common/config/config';
import { BookkeepingComponent } from './bookkeeping/bookkeeping.component';
import { HeaderComponent } from './bookkeeping/header/header.component';
import { HistoryComponent } from './bookkeeping/history/history.component';
import { BudgetComponent } from './bookkeeping/budget/budget.component';
import { SettingsComponent } from './bookkeeping/settings/settings.component';
import { SummaryComponent } from './bookkeeping/summary/summary.component';
import { SpinnerComponent } from './common/components/spinner/spinner.component';
import { CurrencyService } from './common/service/currency.service';
import { CurrencyConversionComponent } from './bookkeeping/summary/currency-conversion/currency-conversion.component';
import { SummaryBodyComponent } from './bookkeeping/summary/summary-body/summary-body.component';
import { SummaryFooterComponent } from './bookkeeping/summary/summary-footer/summary-footer.component';
import { SummaryFooterPipe } from './common/pipes/summary/summary-footer.pipe';
import { CurrencySymbolComponent } from './common/components/currency-symbol/currency-symbol.component';
import { SummaryBalanceOrderingPipe } from './common/pipes/summary/summary-balance-ordering.pipe';
import { CurrencyValuePipe } from './common/pipes/currency-value.pipe';
import { SummaryBodyAccountPipe } from './common/pipes/summary/summary-body-account.pipe';
import { SummaryBodySubAccountPipe } from './common/pipes/summary/summary-body-subaccount.pipe';
import { HistoryService } from './common/service/history.service';
import { HistoryPageActionsComponent } from './bookkeeping/history/history-page-actions/history-page-actions.component';
import { HistoryGroupPipe } from './common/pipes/history-group.pipe';
import { HistoryEditDialogComponent } from './bookkeeping/history/history-edit-dialog/history-edit-dialog.component';
import { ConfirmDialogComponent } from './common/components/confirm-dialog/confirm-dialog.component';
import { ConfirmDialogService } from './common/components/confirm-dialog/confirm-dialog.service';
import { AlertService } from './common/service/alert.service';
import { MyDatePickerModule } from 'mydatepicker';
import { GoalsContainerComponent } from './bookkeeping/history/history-edit-dialog/goals-container/goals-container.component';
import { CurrencyValueDirective } from './common/directives/currency-value.directive';
import { SelectComponent } from './common/components/select/select.component';
import { FocusDirective } from './common/directives/focus.directive';
import { ClickOutsideDirective } from './common/directives/click-outside.directive';
import { SettingsService } from './common/service/settings.service';
import { AssetImagePipe } from './common/pipes/asset-image.pipe';
import { LoadingDialogComponent } from './common/components/loading-dialog/loading-dialog.component';
import { MapKeysPipe } from './common/pipes/map-keys.pipe';
import { InputGroupComponent } from './bookkeeping/history/history-edit-dialog/input-group/input-group.component';
import { AlternativeCurrenciesDialogComponent } from './bookkeeping/history/history-edit-dialog/input-group/alternative-currencies-dialog/alternative-currencies-dialog.component';
import { BudgetService } from './common/service/budget.service';
import { GoalBudgetItemComponent } from './bookkeeping/history/history-edit-dialog/goals-container/goal-budget-item/goal-budget-item.component';
import { TemplateVariableComponent } from './common/components/template-variable/template-variable.component';

@NgModule({
  declarations: [
    BookkeepingRootComponent,
    AuthenticationComponent,
    InputComponent,
    BookkeepingComponent,
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
    ClickOutsideDirective,
    AssetImagePipe,
    LoadingDialogComponent,
    AlternativeCurrenciesDialogComponent,
    MapKeysPipe,
    InputGroupComponent,
    GoalBudgetItemComponent,
    TemplateVariableComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    HttpModule,
    BrowserAnimationsModule,
    MdProgressSpinnerModule,
    MdDialogModule,
    PopoverModule,
    MyDatePickerModule,
    RouterModule.forRoot(BOOKKEEPING_ROUTES),
  ],
  entryComponents: [
    ConfirmDialogComponent,
    HistoryEditDialogComponent,
    LoadingDialogComponent,
    AlternativeCurrenciesDialogComponent
  ],
  providers: [
    SettingsService,
    CurrencyValuePipe,
    AssetImagePipe,
    AuthenticationService,
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
