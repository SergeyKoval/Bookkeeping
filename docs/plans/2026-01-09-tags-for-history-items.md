# Tags for History Items Implementation Plan

**Status:** Implemented (2026-01-09)

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add ability to assign tags to history items (income/expense only), with tag selection in edit dialog and display in history list.

**Architecture:** Tags stored as list of tag titles on HistoryItem. Frontend components for display (TagChipsComponent) and selection (TagSelectorComponent) placed in common for reuse. Tag lookup via Map built from profile tags.

**Tech Stack:** Spring Boot 3.5, MongoDB, Angular 21, Angular Material (Autocomplete, Chips)

---

## Task 1: Backend - Add Tags Field to HistoryItem

**Files:**
- Modify: `backend/src/main/java/by/bk/entity/history/HistoryItem.java`

**Step 1: Add tags field**

Add after line 44 (after `duplicateMessages` field):

```java
    private List<String> tags;
```

**Step 2: Verify compilation**

Run: `cd /Users/skoval/4work/Bookkeeping/backend && mvn compile -q`
Expected: BUILD SUCCESS

---

## Task 2: Frontend - Add Tags Field to HistoryType

**Files:**
- Modify: `frontend/src/app/common/model/history/history-type.ts`

**Step 1: Add tags field to interface**

Add after line 18 (after `deviceMessages` field):

```typescript
  tags?: string[];
```

**Step 2: Verify no syntax errors**

Run: `cd /Users/skoval/4work/Bookkeeping/frontend && npx tsc --noEmit`
Expected: No errors

---

## Task 3: Frontend - Create TagChipsComponent

**Files:**
- Create: `frontend/src/app/common/components/tag/tag-chips/tag-chips.component.ts`
- Create: `frontend/src/app/common/components/tag/tag-chips/tag-chips.component.html`
- Create: `frontend/src/app/common/components/tag/tag-chips/tag-chips.component.css`

**Step 1: Create tag-chips.component.ts**

```typescript
import { Component, Input, OnInit } from '@angular/core';

import { ProfileService } from '../../../service/profile.service';
import { Tag } from '../../../model/tag';

@Component({
    selector: 'bk-tag-chips',
    templateUrl: './tag-chips.component.html',
    styleUrls: ['./tag-chips.component.css'],
    standalone: false
})
export class TagChipsComponent implements OnInit {
  @Input() tagTitles: string[] = [];

  public tagsMap: Map<string, Tag> = new Map();

  public constructor(private _profileService: ProfileService) {}

  public ngOnInit(): void {
    const profileTags = this._profileService.authenticatedProfile?.tags || [];
    profileTags.forEach(tag => this.tagsMap.set(tag.title, tag));
  }

  public getTag(title: string): Tag | undefined {
    return this.tagsMap.get(title);
  }
}
```

**Step 2: Create tag-chips.component.html**

```html
@if (tagTitles && tagTitles.length > 0) {
  <span class="tag-chips">
    @for (title of tagTitles; track title) {
      @if (getTag(title); as tag) {
        <span class="label tag-chip" [style.background-color]="tag.color" [style.color]="tag.textColor">{{tag.title}}</span>
      }
    }
  </span>
}
```

**Step 3: Create tag-chips.component.css**

```css
.tag-chips {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 4px;
}

.tag-chip {
  font-size: 12px;
  font-weight: normal;
  padding: 2px 4px;
}
```

---

## Task 4: Frontend - Create TagSelectorComponent

**Files:**
- Create: `frontend/src/app/common/components/tag/tag-selector/tag-selector.component.ts`
- Create: `frontend/src/app/common/components/tag/tag-selector/tag-selector.component.html`
- Create: `frontend/src/app/common/components/tag/tag-selector/tag-selector.component.css`

**Step 1: Create tag-selector.component.ts**

```typescript
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl } from '@angular/forms';

import { Observable } from 'rxjs';
import { map, startWith } from 'rxjs/operators';

import { ProfileService } from '../../../service/profile.service';
import { Tag } from '../../../model/tag';

@Component({
    selector: 'bk-tag-selector',
    templateUrl: './tag-selector.component.html',
    styleUrls: ['./tag-selector.component.css'],
    standalone: false
})
export class TagSelectorComponent implements OnInit {
  @Input() selectedTags: string[] = [];
  @Output() selectedTagsChange = new EventEmitter<string[]>();

  public searchControl = new FormControl('');
  public filteredTags$: Observable<Tag[]>;
  public allActiveTags: Tag[] = [];
  public tagsMap: Map<string, Tag> = new Map();

  public constructor(private _profileService: ProfileService) {}

  public ngOnInit(): void {
    const profileTags = this._profileService.authenticatedProfile?.tags || [];
    this.allActiveTags = profileTags
      .filter(tag => tag.active)
      .sort((a, b) => a.title.localeCompare(b.title, 'ru'));
    profileTags.forEach(tag => this.tagsMap.set(tag.title, tag));

    this.filteredTags$ = this.searchControl.valueChanges.pipe(
      startWith(''),
      map(value => this.filterTags(value || ''))
    );
  }

  public filterTags(searchValue: string): Tag[] {
    const search = searchValue.toLowerCase();
    return this.allActiveTags.filter(tag =>
      !this.selectedTags.includes(tag.title) &&
      tag.title.toLowerCase().includes(search)
    );
  }

  public selectTag(tag: Tag): void {
    if (!this.selectedTags.includes(tag.title)) {
      this.selectedTags = [...this.selectedTags, tag.title];
      this.selectedTagsChange.emit(this.selectedTags);
    }
    this.searchControl.setValue('');
  }

  public removeTag(title: string): void {
    this.selectedTags = this.selectedTags.filter(t => t !== title);
    this.selectedTagsChange.emit(this.selectedTags);
  }

  public getTag(title: string): Tag | undefined {
    return this.tagsMap.get(title);
  }
}
```

**Step 2: Create tag-selector.component.html**

```html
<div class="tag-selector">
  <div class="selected-tags">
    @for (title of selectedTags; track title) {
      @if (getTag(title); as tag) {
        <span class="label tag-chip" [style.background-color]="tag.color" [style.color]="tag.textColor">
          {{tag.title}}
          <span class="remove-tag" (click)="removeTag(title)">&times;</span>
        </span>
      }
    }
  </div>
  <input
    type="text"
    class="form-control tag-input"
    [formControl]="searchControl"
    placeholder="Добавить тэг..."
    [matAutocomplete]="auto">
  <mat-autocomplete #auto="matAutocomplete" (optionSelected)="selectTag($event.option.value)">
    @for (tag of filteredTags$ | async; track tag.title) {
      <mat-option [value]="tag">
        <span class="label" [style.background-color]="tag.color" [style.color]="tag.textColor">{{tag.title}}</span>
      </mat-option>
    }
  </mat-autocomplete>
</div>
```

**Step 3: Create tag-selector.component.css**

```css
.tag-selector {
  position: relative;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 8px;
}

.tag-chip {
  font-size: 12px;
  font-weight: normal;
  padding: 2px 4px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.remove-tag {
  cursor: pointer;
  font-size: 14px;
  line-height: 1;
  opacity: 0.7;
}

.remove-tag:hover {
  opacity: 1;
}

.tag-input {
  width: 100%;
}
```

---

## Task 5: Frontend - Register Components and Import MatAutocompleteModule

**Files:**
- Modify: `frontend/src/app/bk.module.ts`

**Step 1: Add imports for new components and MatAutocompleteModule**

Add after line 99 (after TagDialogComponent import):

```typescript
import { TagChipsComponent } from './common/components/tag/tag-chips/tag-chips.component';
import { TagSelectorComponent } from './common/components/tag/tag-selector/tag-selector.component';
```

Add after line 11 (after MatTooltipModule import):

```typescript
import { MatAutocompleteModule } from '@angular/material/autocomplete';
```

**Step 2: Add components to declarations**

Add after `TagDialogComponent,` (after line 182):

```typescript
    TagChipsComponent,
    TagSelectorComponent,
```

**Step 3: Add MatAutocompleteModule to imports**

Add after `MatTooltipModule,` (around line 209):

```typescript
    MatAutocompleteModule,
```

---

## Task 6: Frontend - Add Tag Selector to History Edit Dialog

**Files:**
- Modify: `frontend/src/app/history/history-edit-dialog/history-edit-dialog.component.html`
- Modify: `frontend/src/app/history/history-edit-dialog/history-edit-dialog.component.ts`

**Step 1: Add tag selector to template**

Add after line 68 (after the description input div):

```html
      @if (isTypeSelected('expense') || isTypeSelected('income')) {
        <div class="form-row">
          <bk-tag-selector [(selectedTags)]="historyItem.tags"></bk-tag-selector>
        </div>
      }
```

**Step 2: Initialize tags in component**

In `history-edit-dialog.component.ts`, modify `ngOnInit` to initialize tags.

Add after line 72 (after `this.historyItem = ...`):

```typescript
    if (!this.historyItem.tags) {
      this.historyItem.tags = [];
    }
```

**Step 3: Handle tags in initNewHistoryItem method**

Modify the `initNewHistoryItem` method signature (around line 257) to include tags:

Add `historyTags?: string[]` as the last parameter.

Update the result object to include:

```typescript
      'tags': historyTags || [],
```

**Step 4: Update initNewHistoryItemFromExisting method**

In `initNewHistoryItemFromExisting` method (around line 338), add `originalItem.tags` as the last argument to `initNewHistoryItem` call.

---

## Task 7: Frontend - Display Tags in History List

**Files:**
- Modify: `frontend/src/app/history/history.component.html`

**Step 1: Update comment column for expense/income**

Replace lines 149-156 (the expense/income forth column section):

```html
                <!--expense or income forth column-->
                @if (historyItem.type === 'expense' || historyItem.type === 'income') {
                  <div class="col-sm-3 description">
                    @if (historyItem.originalItem.tags && historyItem.originalItem.tags.length > 0) {
                      <div class="history-tags">
                        <bk-tag-chips [tagTitles]="historyItem.originalItem.tags"></bk-tag-chips>
                      </div>
                    }
                    @if (historyItem.description) {
                      <div>{{historyItem.description}}</div>
                    }
                    @if (historyItem.showDeviceMessageIndex != null) {
                      <div class="footnote" [innerHTML]="getFormattedDeviceMessage(historyItem.deviceMessages[historyItem.showDeviceMessageIndex].fullText)"></div>
                    }
                  </div>
                }
```

---

## Task 8: Frontend - Add Styles for Tags in History

**Files:**
- Modify: `frontend/src/app/history/history.component.css`

**Step 1: Add history-tags style**

Add at the end of the file:

```css
.history-tags {
  margin-bottom: 2px;
}
```

---

## Task 9: Verify Frontend Compilation

**Step 1: Build frontend**

Run: `cd /Users/skoval/4work/Bookkeeping/frontend && npm run build`
Expected: Build successful with no errors

---

## Task 10: Manual Testing

**Step 1: Start backend**

Run: `cd /Users/skoval/4work/Bookkeeping/backend && mvn spring-boot:run`

**Step 2: Start frontend (in separate terminal)**

Run: `cd /Users/skoval/4work/Bookkeeping/frontend && npm start`

**Step 3: Test in browser**

1. Navigate to http://localhost:4200
2. Login with test credentials
3. Open history page
4. Click edit on an expense or income item
5. Verify tag selector appears below description field
6. Type to filter tags, select multiple tags
7. Remove a tag by clicking X
8. Save the item
9. Verify tags appear in history list with correct colors
10. Verify tags appear on first line, comment on second line
11. Edit the item again - verify tags are preserved
12. Verify transfer/exchange items don't show tag selector
13. Test creating new expense/income with tags

---

## Summary

**Backend (1 task):**
- Add `tags` field to HistoryItem entity

**Frontend (8 tasks):**
- Add `tags` field to HistoryType interface
- Create TagChipsComponent (read-only display)
- Create TagSelectorComponent (autocomplete + editable chips)
- Register components and import MatAutocompleteModule
- Add tag selector to history edit dialog
- Display tags in history list
- Add CSS for tags display
- Build verification

**Shared components location:** `frontend/src/app/common/components/tag/`
