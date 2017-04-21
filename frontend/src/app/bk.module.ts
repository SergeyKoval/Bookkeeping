import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { BookkeepingComponent } from './bk.component';

@NgModule({
  declarations: [
    BookkeepingComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule
  ],
  providers: [],
  bootstrap: [BookkeepingComponent]
})
export class BookkeepingModule { }
