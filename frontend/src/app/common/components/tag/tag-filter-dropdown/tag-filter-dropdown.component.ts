import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';

import { Tag } from '../../../model/tag';

@Component({
    selector: 'bk-tag-filter-dropdown',
    templateUrl: './tag-filter-dropdown.component.html',
    styleUrls: ['./tag-filter-dropdown.component.css'],
    standalone: false
})
export class TagFilterDropdownComponent implements OnInit, OnChanges {
  @Input() tags: Tag[] = [];
  @Input() selectedTitles: string[] = [];
  @Output() selectionChange = new EventEmitter<string[]>();

  public searchText: string = '';
  public selectedTags: Tag[] = [];
  public unselectedTags: Tag[] = [];

  public ngOnInit(): void {
    this.updateTagLists();
  }

  public ngOnChanges(changes: SimpleChanges): void {
    if (changes['tags'] || changes['selectedTitles']) {
      this.updateTagLists();
    }
  }

  private updateTagLists(): void {
    const activeTags = this.tags.filter(tag => tag.active);
    this.selectedTags = activeTags.filter(tag => this.selectedTitles.includes(tag.title));
    this.unselectedTags = activeTags.filter(tag => !this.selectedTitles.includes(tag.title));
  }

  public getFilteredUnselectedTags(): Tag[] {
    if (!this.searchText) {
      return this.unselectedTags;
    }
    const search = this.searchText.toLowerCase();
    return this.unselectedTags.filter(tag => tag.title.toLowerCase().includes(search));
  }

  public getFilteredSelectedTags(): Tag[] {
    if (!this.searchText) {
      return this.selectedTags;
    }
    const search = this.searchText.toLowerCase();
    return this.selectedTags.filter(tag => tag.title.toLowerCase().includes(search));
  }

  public toggleTag(tag: Tag): void {
    const isSelected = this.selectedTitles.includes(tag.title);
    if (isSelected) {
      this.selectedTitles = this.selectedTitles.filter(t => t !== tag.title);
    } else {
      this.selectedTitles = [...this.selectedTitles, tag.title];
    }
    this.updateTagLists();
    this.selectionChange.emit(this.selectedTitles);
  }

  public isSelected(tag: Tag): boolean {
    return this.selectedTitles.includes(tag.title);
  }

  public getButtonTitle(): string {
    if (this.selectedTitles.length === 0) {
      return 'Тэги';
    }
    return `Тэги (${this.selectedTitles.length})`;
  }

  public clearSearch(): void {
    this.searchText = '';
  }
}
