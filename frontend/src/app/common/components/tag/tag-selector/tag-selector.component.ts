import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl } from '@angular/forms';

import { ProfileService } from '../../../service/profile.service';
import { Tag } from '../../../model/tag';

@Component({
    selector: 'bk-tag-selector',
    templateUrl: './tag-selector.component.html',
    styleUrls: ['./tag-selector.component.css'],
    standalone: false
})
export class TagSelectorComponent implements OnInit {
  @Input() public selectedTags: string[] = [];
  @Output() public selectedTagsChange = new EventEmitter<string[]>();

  public searchControl = new FormControl('');
  public filteredTags: Tag[] = [];
  public allActiveTags: Tag[] = [];
  public tagsMap: Map<string, Tag> = new Map();

  public constructor(private _profileService: ProfileService) {}

  public ngOnInit(): void {
    const profileTags = this._profileService.authenticatedProfile?.tags || [];
    this.allActiveTags = profileTags
      .filter(tag => tag.active)
      .sort((a, b) => a.title.localeCompare(b.title, 'ru'));
    profileTags.forEach(tag => this.tagsMap.set(tag.title, tag));

    this.searchControl.valueChanges.subscribe(() => this.updateFilteredTags());
    this.updateFilteredTags();
  }

  public updateFilteredTags(): void {
    const searchValue = (this.searchControl.value || '').toLowerCase();
    this.filteredTags = this.allActiveTags.filter(tag =>
      !this.selectedTags.includes(tag.title) &&
      tag.title.toLowerCase().includes(searchValue)
    );
  }

  public selectTag(tag: Tag): void {
    if (!this.selectedTags.includes(tag.title)) {
      this.selectedTags = [...this.selectedTags, tag.title];
      this.selectedTagsChange.emit(this.selectedTags);
    }
    this.searchControl.setValue('');
    this.updateFilteredTags();
  }

  public removeTag(title: string): void {
    this.selectedTags = this.selectedTags.filter(t => t !== title);
    this.selectedTagsChange.emit(this.selectedTags);
    this.updateFilteredTags();
  }

  public getTag(title: string): Tag | undefined {
    return this.tagsMap.get(title);
  }
}
