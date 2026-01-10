# Tags Filter for Actions Report - Design

**Status:** Implemented (2026-01-10)

## Overview

Add ability to filter history items by tags in the actions report. Users can select one or more tags, and items matching ANY selected tag are shown (OR logic). Additionally, the settings/tags page report button navigates to the actions report with that tag pre-selected.

## Data Flow

### Request Format

Add `tags` field to report request:
```json
{
  "startPeriod": { "year": 2026, "month": 1, "day": 1 },
  "endPeriod": { "year": 2026, "month": 1, "day": 31 },
  "operations": [...],
  "accounts": [...],
  "tags": ["Groceries", "Subscription"]
}
```

### Backend Filtering

MongoDB query uses `$in` operator for OR logic:
```java
if (tags != null && !tags.isEmpty()) {
    pipes.add(Aggregation.match(Criteria.where("tags").in(tags)));
}
```

An item with `tags: ["Groceries", "Food"]` matches filter `["Groceries", "Travel"]` because "Groceries" is in both.

### Empty Filter Behavior

- Empty `tags` array or null = no tag filtering applied (show all items)
- Matches existing behavior for operations/accounts filters

## Frontend Component: TagFilterDropdownComponent

### Location

`frontend/src/app/common/components/tag/tag-filter-dropdown/`

### Component API

```typescript
@Input() tags: Tag[];                    // Available tags from profile (active only)
@Input() selectedTitles: string[];       // Pre-selected tag titles
@Output() selectionChange: EventEmitter<string[]>;  // Emits selected titles
```

### UI Structure

```
[Тэги ▼]
┌─────────────────────────────┐
│ 🔍 [Поиск...]               │
├─────────────────────────────┤
│ ☑ 🟢 Groceries              │  ← selected tags on top
│ ☑ 🔵 Subscription           │
├─────────────────────────────┤
│ ☐ 🟡 Vacation               │  ← unselected below separator
│ ☐ 🔴 Travel                 │
└─────────────────────────────┘
```

### Features

- Search input filters tags by title (case-insensitive)
- Selected tags shown at top, separated from unselected
- Tag color shown as small chip next to title
- Only active tags displayed
- Checkbox-based selection (glyphicon-check / glyphicon-unchecked)
- Button shows count when tags selected: "Тэги (2)"

## Actions Report Integration

### Filter Placement

After accounts, before period:
```
[Операции ▼] [Счета ▼] [Тэги ▼] [Period] [Поиск]
```

### Tags Display in Results

For expense/income items, tags are displayed in the footnote row:

```
┌──────────┬──────────┬─────────────┬─────────────────────┐
│ Date     │ Value    │ Account     │ Category            │
├──────────┴──────────┼─────────────┴─────────────────────┤
│ [Tag1] [Tag2]       │ Комментарий: ...                  │
│ Цель: ...           │                                   │
└─────────────────────┴───────────────────────────────────┘
```

- Left side (col-sm-4): Tags (using bk-tag-chips), then Goal (if present)
- Right side (col-sm-8): Comment (if present)
- Row shown when any of: tags, goal, or description exist

### Query Parameter Support

URL: `/reports/actions?tag=TagTitle`

On component init:
1. Read `tag` query param
2. If present, pre-select that tag in filter
3. Do NOT auto-trigger search

### Validation

Tag filter is optional - no validation error if empty (unlike operations/accounts which require selection).

## Navigation from Settings/Tags

### Report Button Action

When user clicks report button (📊) on a tag row:
```typescript
this._router.navigate(['/reports/actions'], {
  queryParams: { tag: tag.title }
});
```

### Behavior

- Opens actions report page
- Tag is pre-selected in filter dropdown
- Search not triggered automatically
- User can modify filters and click "Поиск"

## Files Created

| File | Purpose |
|------|---------|
| `frontend/.../tag/tag-filter-dropdown/tag-filter-dropdown.component.ts` | Component logic |
| `frontend/.../tag/tag-filter-dropdown/tag-filter-dropdown.component.html` | Template |
| `frontend/.../tag/tag-filter-dropdown/tag-filter-dropdown.component.css` | Styling |

## Files Modified

| File | Change |
|------|--------|
| `backend/.../ReportRequest.java` | Add `List<String> tags` field |
| `backend/.../HistoryAPI.java` | Add `tags` parameter to `getFiltered()` |
| `backend/.../HistoryService.java` | Add tag filtering with OR logic |
| `backend/.../ReportController.java` | Pass tags to service |
| `frontend/.../report-actions.component.ts` | Add tagsFilter, availableTags, read query param |
| `frontend/.../report-actions.component.html` | Add tag-filter-dropdown, display tags in results |
| `frontend/.../report-actions.component.css` | Add `.report-tags` style |
| `frontend/.../report.service.ts` | Add tags to API request |
| `frontend/.../tags.component.ts` | Implement router navigation |
| `frontend/.../bk.module.ts` | Register TagFilterDropdownComponent |

## Out of Scope

- Tag filtering in summary/dynamic reports
- Selecting multiple tags from settings page navigation
- AND logic for tag filtering
