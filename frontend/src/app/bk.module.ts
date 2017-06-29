import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import { HttpModule } from '@angular/http';
import {RouterModule} from '@angular/router';
import {MdDialogModule, MdProgressSpinnerModule, MdTabsModule} from '@angular/material';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

import {PopoverModule} from 'ngx-popover';

import {BookkeepingRootComponent} from './bk.component';
import {BOOKKEEPING_ROUTES} from './routes';
import { AuthenticationComponent } from './authentication/authentication.component';
import { InputComponent } from './authentication/input/input.component';
import {AuthenticationService} from './common/service/authentication.service';
import {LoadingService} from './common/service/loading.service';
import {environment} from '../environments/environment';
import {HOST} from './common/config/config';
import {BookkeepingComponent} from './bookkeeping/bookkeeping.component';
import { HeaderComponent } from './bookkeeping/header/header.component';
import { HistoryComponent } from './bookkeeping/history/history.component';
import { BudgetComponent } from './bookkeeping/budget/budget.component';
import { SettingsComponent } from './bookkeeping/settings/settings.component';
import { SummaryComponent } from './bookkeeping/summary/summary.component';
import { SpinnerComponent } from './common/components/spinner/spinner.component';
import {CurrencyService} from './common/service/currency.service';
import { CurrencyConversionComponent } from './bookkeeping/summary/currency-conversion/currency-conversion.component';
import {SummaryService} from './common/service/summary.service';
import { SummaryBodyComponent } from './bookkeeping/summary/summary-body/summary-body.component';
import { SummaryFooterComponent } from './bookkeeping/summary/summary-footer/summary-footer.component';
import { SummaryFooterPipe } from './common/pipes/summary/summary-footer.pipe';
import { CurrencySymbolComponent } from './common/components/currency-symbol/currency-symbol.component';
import { SummaryBalanceOrderingPipe } from './common/pipes/summary/summary-balance-ordering.pipe';
import { CurrencyValuePipe } from './common/pipes/currency-value.pipe';
import { SummaryBodyCategoryPipe } from './common/pipes/summary/summary-body-category.pipe';
import { SummaryBodySubcategoryPipe } from './common/pipes/summary/summary-body-subcategory.pipe';
import {HistoryService} from './common/service/history.service';
import { HistoryPageActionsComponent } from './bookkeeping/history/history-page-actions/history-page-actions.component';
import { HistoryGroupPipe } from './common/pipes/history-group.pipe';
import { HistoryEditPopupComponent } from './bookkeeping/history/history-edit-popup/history-edit-popup.component';
import { ConfirmPopupComponent } from './common/components/confirm-popup/confirm-popup.component';
import {ConfirmPopupService} from './common/components/confirm-popup/confirm-popup.service';
import {AlertService} from './common/service/alert.service';
import {MyDatePickerModule} from 'mydatepicker';
import { GoalsContainerComponent } from './bookkeeping/history/history-edit-popup/goals-container/goals-container.component';
import { CurrencyValueDirective } from './common/directives/currency-value.directive';
import { SelectComponent } from './common/components/select/select.component';
import { FocusDirective } from './common/directives/focus.directive';
import { ClickOutsideDirective } from './common/directives/click-outside.directive';

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
    SummaryBodyCategoryPipe,
    SummaryBodySubcategoryPipe,
    HistoryPageActionsComponent,
    HistoryGroupPipe,
    HistoryEditPopupComponent,
    ConfirmPopupComponent,
    GoalsContainerComponent,
    CurrencyValueDirective,
    SelectComponent,
    FocusDirective,
    ClickOutsideDirective
  ],
  imports: [
    BrowserModule,
    ReactiveFormsModule,
    HttpModule,
    BrowserAnimationsModule,
    MdProgressSpinnerModule,
    MdDialogModule,
    PopoverModule,
    MyDatePickerModule,
    MdTabsModule,
    RouterModule.forRoot(BOOKKEEPING_ROUTES),
  ],
  entryComponents: [
    ConfirmPopupComponent,
    HistoryEditPopupComponent
  ],
  providers: [
    CurrencyValuePipe,
    AuthenticationService,
    LoadingService,
    CurrencyService,
    SummaryService,
    HistoryService,
    ConfirmPopupService,
    AlertService,
    {
      provide: HOST,
      useValue: environment.backendHost,
    }
  ],
  bootstrap: [BookkeepingRootComponent]
})
export class BookkeepingModule { }
