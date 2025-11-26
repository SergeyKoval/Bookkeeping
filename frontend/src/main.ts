import { enableProdMode, provideZoneChangeDetection } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { BookkeepingModule } from './app/bk.module';
import { environment } from './environments/environment';

if (environment.production) {
  enableProdMode();
}

platformBrowserDynamic().bootstrapModule(BookkeepingModule, { applicationProviders: [provideZoneChangeDetection()], }).catch(err => console.log(err));
