import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'bk-bookkeeping',
  templateUrl: './bookkeeping.component.html',
  styleUrls: ['./bookkeeping.component.css']
})
export class BookkeepingComponent implements OnInit {
  public currencies: Currency[];

  public constructor(private _ar: ActivatedRoute) {}

  public ngOnInit(): void {
    this._ar.data.subscribe((data: {currencies: Currency[]}) => {
      this.currencies = data.currencies;
    });
  }
}
