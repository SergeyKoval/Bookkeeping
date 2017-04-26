import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import { HttpModule } from '@angular/http';
import {RouterModule} from '@angular/router';
import {MdProgressSpinnerModule} from '@angular/material';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

import {BookkeepingRootComponent} from './bk.component';
import {BOOKKEEPING_ROUTES} from './routes';
import { AuthenticationComponent } from './authentication/authentication.component';
import { InputComponent } from './authentication/input/input.component';
import {AuthenticationService} from './authentication/authentication.service';
import {LoadingService} from './common/service/loading.service';
import {environment} from '../environments/environment';
import {HOST} from './common/config/config';
import {BookkeepingComponent} from './bookkeeping/bookkeeping.component';
import { HeaderComponent } from './bookkeeping/header/header.component';
import { HistoryComponent } from './bookkeeping/history/history.component';
import { BudgetComponent } from './bookkeeping/budget/budget.component';
import { SettingsComponent } from './bookkeeping/settings/settings.component';

@NgModule({
  declarations: [
    BookkeepingRootComponent,
    AuthenticationComponent,
    InputComponent,
    BookkeepingComponent,
    HeaderComponent,
    HistoryComponent,
    BudgetComponent,
    SettingsComponent
  ],
  imports: [
    BrowserModule,
    ReactiveFormsModule,
    HttpModule,
    BrowserAnimationsModule,
    MdProgressSpinnerModule,
    RouterModule.forRoot(BOOKKEEPING_ROUTES),
  ],
  providers: [
    AuthenticationService,
    LoadingService,
    {
      provide: HOST,
      useValue: environment.backendHost,
    }
  ],
  bootstrap: [BookkeepingRootComponent]
})
export class BookkeepingModule { }
