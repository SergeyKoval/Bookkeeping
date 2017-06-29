import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';

import {FocusDirective} from '../../directives/focus.directive';

@Component({
  selector: 'bk-select',
  templateUrl: './select.component.html',
  styleUrls: ['./select.component.css']
})
export class SelectComponent implements OnInit {
  @Input()
  public width: number;
  @Input()
  public placeholder: string;
  @Input()
  public items: SelectItem[];
  @Input()
  public selectedItem: string[] = [];
  @Output()
  public changeValue: EventEmitter<string[]> = new EventEmitter();

  public searchValue: string = '';
  public opened: boolean = false;
  public displayItems: SelectItem[];
  private originalItem: string[];
  @ViewChild(FocusDirective)
  private searchInput: FocusDirective;

  public ngOnInit(): void {
    if (!this.selectedItem || this.selectedItem.length === 0) {
      this.switchToRootLevel();
    } else {
      this.originalItem = this.selectedItem;
      this.displayItems = this.items.filter((item: SelectItem) => item.title === this.selectedItem[0])[0].children;
    }
  }

  public chooseItem(item: SelectItem): void {
    if (item.children) {
      this.selectedItem = [item.title];
      this.displayItems = item.children;
      this.searchValue = '';
      return;
    }

    if (item.parent) {
      this.selectedItem = [item.parent.title, item.title];
    }  else {
      this.selectedItem[1] = item.title;
    }
    this.changeValue.emit(this.selectedItem);
    this.closeSelect();
    this.ngOnInit();
  }

  public openSelect(): void {
    this.searchValue = '';
    this.opened = true;
  }

  public closeSelect(): void {
    if (!this.selectedItem || this.selectedItem.length < 2) {
      this.selectedItem = this.originalItem;
    }
    this.opened = false;
    this.ngOnInit();
  }

  public isItemSelected(): boolean {
    return this.selectedItem && this.selectedItem.length > 0;
  }

  public switchToRootLevel(): void {
    this.displayItems = this.items;
    this.selectedItem = [];
    this.searchValue = '';
  }

  public focusInput(): void {
    if (this.searchInput) {
      this.searchInput.ngOnInit();
    }
  }

  public search(inputValue: string): void {
    this.searchValue = inputValue;
    const value: string = inputValue.toLowerCase();
    if (value === '') {
      this.ngOnInit();
      return;
    }

    if (this.selectedItem && this.selectedItem.length > 0) {
      const parentTitle: string = this.selectedItem[0];
      this.displayItems = this.items
        .filter((item: SelectItem) => item.title === parentTitle)[0]
        .children.filter((item: SelectItem) => item.title.toLowerCase().startsWith(value));
    } else {
      const searchItems: SelectItem[] = [];
      this.items.forEach((item: SelectItem) => {
        if (item.title.toLowerCase().startsWith(value)) {
          searchItems.push(item);
        }
        if (item.children) {
          item.children.forEach((childItem: SelectItem) => {
            if (childItem.title.toLowerCase().startsWith(value)) {
              searchItems.push({title: childItem.title, parent: item});
            }
          });
        }
      });
      this.displayItems = searchItems;
    }
  }
}
