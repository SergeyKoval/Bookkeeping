import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import { HttpModule } from '@angular/http';
import {RouterModule} from '@angular/router';
import {MdProgressSpinnerModule} from '@angular/material';
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
    SummaryBodySubcategoryPipe
  ],
  imports: [
    BrowserModule,
    ReactiveFormsModule,
    HttpModule,
    BrowserAnimationsModule,
    MdProgressSpinnerModule,
    PopoverModule,
    RouterModule.forRoot(BOOKKEEPING_ROUTES),
  ],
  providers: [
    AuthenticationService,
    LoadingService,
    CurrencyService,
    SummaryService,
    {
      provide: HOST,
      useValue: environment.backendHost,
    }
  ],
  bootstrap: [BookkeepingRootComponent]
})
export class BookkeepingModule { }
