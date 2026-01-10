# Tags for History Items - Design

**Status:** Implemented (2026-01-09)

## Overview

Add ability to assign tags to history items (income/expense only). Tags are selected in the add/edit dialog and displayed in the history list.

## Data Model

### Backend - HistoryItem.java

Add field:
```java
private List<String> tags;  // list of tag titles, nullable
```

### Frontend - history-type.ts

Add to interface:
```typescript
tags?: string[];  // list of tag titles
```

### Tag Lookup

Frontend builds `Map<title, Tag>` from `ProfileService.authenticatedProfile.tags` for O(1) lookup when rendering.

### Orphan Handling

If a tag title in history item doesn't exist in user's tags (was deleted), skip rendering it.

## API Changes

No new endpoints. Existing history endpoints accept optional `tags` field:

- `POST /api/history` - include optional `tags: string[]`
- `PUT /api/history` - include optional `tags: string[]`

## Frontend Components

### TagChipsComponent (read-only display)

Location: `common/components/tag/tag-chips/`

```typescript
@Input() tagTitles: string[] = [];
```

- Looks up each title in profile tags
- Renders chips with `color` (background) and `textColor`
- Skips titles not found (deleted tags)
- Styling matches settings/tags page

### TagSelectorComponent (editable)

Location: `common/components/tag/tag-selector/`

```typescript
@Input() selectedTags: string[] = [];
@Output() selectedTagsChange = EventEmitter<string[]>();
```

- Autocomplete input field
- Typing filters tags by title (case-insensitive, contains match)
- Only shows **active** tags in dropdown
- Click tag to add as colored chip
- Click chip's X to remove
- Uses TagChipsComponent for display

## UI Integration

### History Edit Dialog

- Tag selector appears below description field
- Only shown for expense/income types (not transfer/exchange)

### History List

Comment column for income/expense rows:
```
┌─────────────────────────────┐
│ [Tag1] [Tag2] [Tag3]        │  ← colored chips (if tags exist)
│ Original comment text here  │  ← comment (if exists)
└─────────────────────────────┘
```

When no tags: show only comment (as today).

## Files to Create

| File | Purpose |
|------|---------|
| `common/components/tag/tag-chips/tag-chips.component.ts` | Shared chip display |
| `common/components/tag/tag-chips/tag-chips.component.html` | Template |
| `common/components/tag/tag-chips/tag-chips.component.css` | Styling |
| `common/components/tag/tag-selector/tag-selector.component.ts` | Autocomplete selector |
| `common/components/tag/tag-selector/tag-selector.component.html` | Template |
| `common/components/tag/tag-selector/tag-selector.component.css` | Styling |

## Files to Modify

| File | Change |
|------|--------|
| `backend/.../HistoryItem.java` | Add `tags` field |
| `frontend/.../history-type.ts` | Add `tags?: string[]` |
| `frontend/.../history-edit-dialog.component.html` | Add tag-selector |
| `frontend/.../history-edit-dialog.component.ts` | Handle tags in save |
| `frontend/.../history.component.html` | Add tag-chips display |
| `frontend/.../bk.module.ts` | Register new components |

## Out of Scope

- Tags for transfer/exchange history items
- ~~Tag filtering in reports (future work)~~ → Implemented in 2026-01-10-tags-for-actions-report
