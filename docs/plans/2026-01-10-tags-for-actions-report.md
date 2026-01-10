# Tags Filter for Actions Report Implementation Plan

**Status:** Implemented (2026-01-10)

**Goal:** Add tag filtering capability to the actions report with OR logic, plus navigation from settings/tags page to pre-select a tag.

**Architecture:** Frontend creates a new TagFilterDropdownComponent with search and selected-on-top features. Backend adds `tags` field to ReportRequest and filters using MongoDB `$in` operator for OR logic. Tags are displayed in report results using existing TagChipsComponent.

**Tech Stack:** Spring Boot 3.5, MongoDB, Angular 21, Angular Material

---

## Task 1: Backend - Add Tags Field to ReportRequest

**Files:**
- Modify: `backend/src/main/java/by/bk/controller/model/request/ReportRequest.java`

**Step 1: Add tags field**

Add after line 15 (after `currencies` field):

```java
    private List<String> tags;
```

**Step 2: Verify compilation**

Run: `cd /Users/skoval/4work/Bookkeeping/backend && mvn compile -q`
Expected: BUILD SUCCESS

---

## Task 2: Backend - Update HistoryAPI Interface

**Files:**
- Modify: `backend/src/main/java/by/bk/entity/history/HistoryAPI.java`

**Step 1: Add tags parameter to getFiltered method**

Replace line 31:

```java
    List<HistoryItem> getFiltered(String login, DateRequest startPeriod, DateRequest endPeriod, List<List<String>> operations, List<List<String>> accounts, List<String> tags);
```

**Step 2: Verify compilation**

Run: `cd /Users/skoval/4work/Bookkeeping/backend && mvn compile -q`
Expected: FAIL (HistoryService not updated yet)

---

## Task 3: Backend - Implement Tag Filtering in HistoryService

**Files:**
- Modify: `backend/src/main/java/by/bk/entity/history/HistoryService.java`

**Step 1: Update getFiltered method signature**

Replace line 218:

```java
    public List<HistoryItem> getFiltered(String login, DateRequest startPeriod, DateRequest endPeriod, List<List<String>> operations, List<List<String>> accounts, List<String> tags) {
```

**Step 2: Add tag filtering logic**

Add after line 231 (after accounts filtering block, before the sort):

```java
        if (tags != null && !tags.isEmpty()) {
            pipes.add(Aggregation.match(Criteria.where("tags").in(tags)));
        }
```

**Step 3: Verify compilation**

Run: `cd /Users/skoval/4work/Bookkeeping/backend && mvn compile -q`
Expected: FAIL (ReportController not updated yet)

---

## Task 4: Backend - Update ReportController

**Files:**
- Modify: `backend/src/main/java/by/bk/controller/ReportController.java`

**Step 1: Pass tags to service**

Replace line 26:

```java
        return historyAPI.getFiltered(principal.getName(), request.getStartPeriod(), request.getEndPeriod(), request.getOperations(), request.getAccounts(), request.getTags());
```

**Step 2: Verify backend compilation**

Run: `cd /Users/skoval/4work/Bookkeeping/backend && mvn compile -q`
Expected: BUILD SUCCESS

---

## Task 5: Frontend - Create TagFilterDropdownComponent TypeScript

**Files:**
- Create: `frontend/src/app/common/components/tag/tag-filter-dropdown/tag-filter-dropdown.component.ts`

**Step 1: Create the component file**

```typescript
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
```

---

## Task 6: Frontend - Create TagFilterDropdownComponent Template

**Files:**
- Create: `frontend/src/app/common/components/tag/tag-filter-dropdown/tag-filter-dropdown.component.html`

**Step 1: Create the template file**

```html
<div class="dropdown">
  <a role="button" data-toggle="dropdown" class="btn btn-default" data-target="#">
    {{getButtonTitle()}} <span class="caret"></span>
  </a>
  <ul class="dropdown-menu tag-filter-menu" role="menu" (click)="$event.stopPropagation()">
    <li class="search-container">
      <input
        type="text"
        class="form-control search-input"
        [(ngModel)]="searchText"
        placeholder="Поиск..."
        (click)="$event.stopPropagation()">
    </li>

    @if (getFilteredSelectedTags().length > 0) {
      @for (tag of getFilteredSelectedTags(); track tag.title) {
        <li>
          <a (click)="toggleTag(tag)">
            <span class="checkbox-icon glyphicon glyphicon-check"></span>
            <span class="tag-color" [style.background-color]="tag.color"></span>
            <span class="tag-title">{{tag.title}}</span>
          </a>
        </li>
      }
      <li role="separator" class="divider"></li>
    }

    @for (tag of getFilteredUnselectedTags(); track tag.title) {
      <li>
        <a (click)="toggleTag(tag)">
          <span class="checkbox-icon glyphicon glyphicon-unchecked"></span>
          <span class="tag-color" [style.background-color]="tag.color"></span>
          <span class="tag-title">{{tag.title}}</span>
        </a>
      </li>
    }

    @if (getFilteredSelectedTags().length === 0 && getFilteredUnselectedTags().length === 0) {
      <li class="no-tags">
        <span>Тэги не найдены</span>
      </li>
    }
  </ul>
</div>
```

---

## Task 7: Frontend - Create TagFilterDropdownComponent Styles

**Files:**
- Create: `frontend/src/app/common/components/tag/tag-filter-dropdown/tag-filter-dropdown.component.css`

**Step 1: Create the styles file**

```css
.tag-filter-menu {
  min-width: 200px;
  max-height: 300px;
  overflow-y: auto;
  padding-top: 0;
}

.search-container {
  padding: 8px;
  border-bottom: 1px solid #e5e5e5;
}

.search-input {
  width: 100%;
}

.tag-filter-menu li > a {
  display: flex;
  align-items: center;
  padding: 6px 12px;
  cursor: pointer;
}

.tag-filter-menu li > a:hover {
  background-color: #f5f5f5;
}

.checkbox-icon {
  margin-right: 8px;
  color: #333;
}

.tag-color {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 2px;
  margin-right: 8px;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.tag-title {
  flex: 1;
}

.no-tags {
  padding: 12px;
  text-align: center;
  color: #999;
}

.divider {
  margin: 4px 0;
}
```

---

## Task 8: Frontend - Register TagFilterDropdownComponent in Module

**Files:**
- Modify: `frontend/src/app/bk.module.ts`

**Step 1: Add import**

Add after line 102 (after TagSelectorComponent import):

```typescript
import { TagFilterDropdownComponent } from './common/components/tag/tag-filter-dropdown/tag-filter-dropdown.component';
```

**Step 2: Add to declarations**

Add after line 187 (after TagSelectorComponent in declarations):

```typescript
    TagFilterDropdownComponent,
```

**Step 3: Verify no syntax errors**

Run: `cd /Users/skoval/4work/Bookkeeping/frontend && npx tsc --noEmit`
Expected: No errors (or only pre-existing errors)

---

## Task 9: Frontend - Update ReportService

**Files:**
- Modify: `frontend/src/app/common/service/report.service.ts`

**Step 1: Add tags parameter to method**

Replace lines 23-32:

```typescript
  public getHistoryItemsForPeriodReport(periodFilter: PeriodFilter, operationsFilter: MultiLevelDropdownItem[], accountsFilter: MultiLevelDropdownItem[], tags: string[] = []): Observable<HistoryType[]> {
    const selectedOperations: string[][] = this.prepareFilteredItems(operationsFilter);
    const selectedAccounts: string[][] = this.prepareFilteredItems(accountsFilter);
    return this._http.post<HistoryType[]>('/api/report/history-actions', {
      'startPeriod': periodFilter.startDate,
      'endPeriod': periodFilter.endDate,
      'operations': selectedOperations,
      'accounts': selectedAccounts,
      'tags': tags
    });
  }
```

---

## Task 10: Frontend - Update ReportActionsComponent TypeScript

**Files:**
- Modify: `frontend/src/app/reports/report-actions/report-actions.component.ts`

**Step 1: Add imports**

Add after line 1:

```typescript
import { ActivatedRoute } from '@angular/router';
```

Add after line 12 (after Tag import from history-type, add Tag model import):

```typescript
import { Tag } from '../../common/model/tag';
```

**Step 2: Add properties**

Add after line 38 (after historyItems property):

```typescript
  public tagsFilter: string[] = [];
  public availableTags: Tag[] = [];
```

**Step 3: Inject ActivatedRoute**

Add `private _route: ActivatedRoute` to constructor parameters (after line 50, before the closing parenthesis):

Replace lines 40-53:

```typescript
  public constructor(
    protected _profileService: ProfileService,
    protected _imagePipe: AssetImagePipe,
    private _alertService: AlertService,
    private _dialog: MatDialog,
    private _confirmDialogService: ConfirmDialogService,
    private _loadingService: LoadingService,
    private _budgetService: BudgetService,
    private _historyService: HistoryService,
    private _authenticationService: ProfileService,
    private _reportService: ReportService,
    private _route: ActivatedRoute
  ) {
    super(_profileService, _imagePipe);
  }
```

**Step 4: Update ngOnInit**

Replace lines 55-59:

```typescript
  public ngOnInit(): void {
    const profile: Profile = this._profileService.authenticatedProfile;
    this.operationsFilter = this.populateCategoriesFilter(profile.categories);
    this.accountsFilter = this.populateAccountsFilter(profile.accounts);
    this.availableTags = (profile.tags || []).filter(tag => tag.active);

    const tagParam = this._route.snapshot.queryParamMap.get('tag');
    if (tagParam) {
      this.tagsFilter = [tagParam];
    }
  }
```

**Step 5: Update search method**

Replace line 78:

```typescript
    this._reportService.getHistoryItemsForPeriodReport(this.periodFilter, this.operationsFilter, this.accountsFilter, this.tagsFilter).subscribe((items: HistoryType[]) => {
```

---

## Task 11: Frontend - Update ReportActionsComponent Template

**Files:**
- Modify: `frontend/src/app/reports/report-actions/report-actions.component.html`
- Modify: `frontend/src/app/reports/report-actions/report-actions.component.css`

**Step 1: Add tag filter dropdown to filter row**

Add after the accounts dropdown, before period filter:

```html
<bk-tag-filter-dropdown class="filter" [tags]="availableTags" [selectedTitles]="tagsFilter" (selectionChange)="tagsFilter = $event"></bk-tag-filter-dropdown>
```

**Step 2: Display tags in results footnote row**

Update the expense/income footnote row condition and content to include tags:

```html
@if (historyItem.goal || historyItem.description || (historyItem.originalItem.tags && historyItem.originalItem.tags.length > 0)) {
  <div class="row footnote-row">
    <div class="col-sm-4">
      @if (historyItem.originalItem.tags && historyItem.originalItem.tags.length > 0) {
        <bk-tag-chips [tagTitles]="historyItem.originalItem.tags"></bk-tag-chips>
      }
      @if (historyItem.goal) {
        <span><strong>Цель:</strong> {{historyItem.goal}}</span>
      }
    </div>
    <div class="col-sm-8">@if (historyItem.description) {
      <span><strong>Комментарий:</strong> {{historyItem.description}}</span>
    }</div>
  </div>
}
```

**Step 3: Add CSS for tags in results**

Add to `report-actions.component.css`:

```css
.report-tags {
  margin-top: 2px;
}
```

---

## Task 12: Frontend - Update TagsComponent for Navigation

**Files:**
- Modify: `frontend/src/app/settings/tags/tags.component.ts`

**Step 1: Add Router import**

Add after line 2:

```typescript
import { Router } from '@angular/router';
```

**Step 2: Inject Router**

Replace lines 24-30:

```typescript
  public constructor(
    private _profileService: ProfileService,
    private _dialog: MatDialog,
    private _loadingService: LoadingService,
    private _alertService: AlertService,
    private _confirmDialogService: ConfirmDialogService,
    private _router: Router
  ) { }
```

**Step 3: Implement openReport method**

Replace lines 112-114:

```typescript
  public openReport(tag: Tag): void {
    this._router.navigate(['/reports/actions'], {
      queryParams: { tag: tag.title }
    });
  }
```

---

## Task 13: Verify Frontend Compilation

**Step 1: Build frontend**

Run: `cd /Users/skoval/4work/Bookkeeping/frontend && npm run build`
Expected: Build successful with no errors

---

## Task 14: Manual Testing

**Step 1: Start backend**

Run: `cd /Users/skoval/4work/Bookkeeping/backend && mvn spring-boot:run`

**Step 2: Start frontend (in separate terminal)**

Run: `cd /Users/skoval/4work/Bookkeeping/frontend && npm start`

**Step 3: Test tag filter in actions report**

1. Navigate to http://localhost:4200
2. Login with test credentials
3. Go to Reports > Операции за период
4. Verify "Тэги" dropdown appears after "Счета"
5. Open tag dropdown - verify search input at top
6. Select a tag - verify it moves to top section
7. Select multiple tags
8. Run search with tags selected
9. Verify only items with matching tags appear
10. Clear tags filter, verify all items appear

**Step 4: Test navigation from settings**

1. Go to Settings > Тэги
2. Click report button (📊) on any tag
3. Verify navigation to /reports/actions?tag=TagTitle
4. Verify tag is pre-selected in dropdown
5. Verify search is NOT auto-triggered
6. Click "Поиск" - verify results filtered by tag

**Step 5: Test edge cases**

1. Test with no tags in profile - dropdown should show "Тэги не найдены"
2. Test search filtering in dropdown
3. Test OR logic: item with tags [A, B] should appear when filtering by [A] or [B] or [A, C]

---

## Summary

**Backend (4 tasks):**
- Add `tags` field to ReportRequest
- Add `tags` parameter to HistoryAPI.getFiltered()
- Implement tag filtering in HistoryService with MongoDB `$in` operator
- Pass tags through ReportController

**Frontend (9 tasks):**
- Create TagFilterDropdownComponent (ts, html, css) with search and selected-on-top features
- Register component in module
- Update ReportService to include tags in API call
- Update ReportActionsComponent:
  - Add tag filter dropdown (after accounts, before period)
  - Display tags in results footnote row (left side with bk-tag-chips)
  - Read query param for pre-selection
- Update TagsComponent navigation with Router

**Files Changed (13 total):**
- 4 backend files (ReportRequest, HistoryAPI, HistoryService, ReportController)
- 3 new frontend files (TagFilterDropdownComponent ts/html/css)
- 6 modified frontend files (bk.module, report.service, report-actions ts/html/css, tags.component)
