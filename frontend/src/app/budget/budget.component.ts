import { Component, OnInit } from '@angular/core';
import { DateUtils } from '../common/utils/date-utils';

@Component({
  selector: 'bk-budget',
  templateUrl: './budget.component.html',
  styleUrls: ['./budget.component.css']
})
export class BudgetComponent implements OnInit {
  public loading: boolean;
  public selectedMonth: number;
  public selectedYear: number;
  public months: string[] = DateUtils.MONTHS;

  public items: SelectItem[];
  public items2: SelectItem[];
  public selectedItem: SelectItem[];
  public selectedItem2: SelectItem[];

  public constructor() {}

  public ngOnInit(): void {
    this.items = [
      {title: 'Деньги', icon: 'assets/image/other_exp.gif', children: [{title: 'Наличные'}, {title: 'Резерв'}]},
      {title: 'Сбережения', icon: 'assets/image/other_exp.gif', children: [{title: 'Белинвест', icon: 'assets/image/home.gif'}, {title: 'Альфа', icon: 'assets/image/home.gif'}]},
      {title: 'Абс', icon: 'assets/image/other_exp.gif', children: [{title: 'Аааа'}, {title: 'Бббб'}]}
    ];

    this.items2 = [
      {title: 'Деньги2', icon: 'assets/image/other_exp.gif', children: [{title: 'Наличные2'}, {title: 'Резерв2'}]},
      {title: 'Сбережения2', icon: 'assets/image/other_exp.gif', children: [{title: 'Белинвест2', icon: 'assets/image/home.gif'}, {title: 'Альфа2', icon: 'assets/image/home.gif'}]},
      {title: 'Абс2', icon: 'assets/image/other_exp.gif', children: [{title: 'Аааа2'}, {title: 'Бббб2'}]}
    ];

    this.selectedItem = [{title: 'Сбережения', icon: 'assets/image/other_exp.gif', children: [{title: 'Белинвест', icon: 'assets/image/home.gif'},
      {title: 'Альфа', icon: 'assets/image/home.gif'}]}, {title: 'Альфа', icon: 'assets/image/home.gif'}];

    this.selectedItem2 = [{title: 'Сбережения2', icon: 'assets/image/other_exp.gif', children: [{title: 'Белинвест2', icon: 'assets/image/home.gif'},
      {title: 'Альфа2', icon: 'assets/image/home.gif'}]}, {title: 'Альфа2', icon: 'assets/image/home.gif'}];
  }
}
