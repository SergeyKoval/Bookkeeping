# Tags Feature Design

**Status:** Implemented (2026-01-05)

## Overview

Add a tagging system to group history items by user-defined tags. Tags are managed via a new settings page and can be assigned to individual history items (multiple tags per item supported).

## Data Model

### Backend

**Tag.java** (new entity):
```java
public class Tag {
    private String id;       // UUID, generated on creation
    private String title;
    private String color;    // hex color like "#FF5733"
    private boolean active;  // default true
}
```

**User.java** (addition):
```java
private List<Tag> tags;  // embedded in User document
```

**HistoryItem.java** (addition):
```java
private List<String> tags;  // list of tag IDs
```

### Frontend

Mirror backend models. Tags sorted alphabetically by title when displayed.

Build `Map<id, Tag>` from profile for efficient lookup when rendering history items.

## API Endpoints

| Endpoint | Method | Request Body | Description |
|----------|--------|--------------|-------------|
| `/api/profile/tag` | POST | `{title, color}` | Add new tag (generates UUID, active=true) |
| `/api/profile/tag` | PUT | `{id, title, color, active}` | Update tag |
| `/api/profile/tag/{id}` | DELETE | - | Delete tag |

### Validation Rules

- Title must be unique (case-insensitive)
- Title required, non-empty
- Color required, valid hex format

### Delete Behavior

Tag is removed from User document. Orphaned tag IDs in history items are ignored during render (no cleanup needed).

### Response Format

Standard `SimpleResponse`:
```json
{
  "status": "SUCCESS|FAIL",
  "message": "ALREADY_EXIST|..."  // optional error detail
}
```

## Frontend UI

### Route

`/settings/tags` - new tab "Тэги" in settings navigation

### Tags List Page

```
┌─────────────────────────────────────────────────────┐
│  [Profile] [Currencies] [Accounts] [Categories]     │
│  [Devices] [Тэги]                                   │
├─────────────────────────────────────────────────────┤
│                                                     │
│  [+ Добавить тэг]                                   │
│                                                     │
│  ┌─────────────────────────────────────────────┐   │
│  │ ██ Groceries     [📊] [✏️] [🗑️] [toggle]  │   │
│  │ ██ Subscription  [📊] [✏️] [🗑️] [toggle]  │   │
│  │ ░░ Vacation      [📊] [✏️] [🗑️] [toggle]  │   │  <- grayed (inactive)
│  └─────────────────────────────────────────────┘   │
│                                                     │
└─────────────────────────────────────────────────────┘
```

### Tag Row Elements

- Color chip (small colored square)
- Title
- Report button - disabled placeholder (future: opens actions report with tag filter)
- Edit button - opens add/edit dialog
- Delete button - shows confirmation before delete
- Active toggle (switch)

### Inactive Tag Behavior

- Shown grayed out in all views
- Cannot be assigned to new history items
- Existing assignments remain visible (grayed)

### Add/Edit Dialog

- Title input field (required)
- Color picker: preset palette (8-12 colors) + custom hex input option
- Save/Cancel buttons
- Validation feedback for duplicate titles

## Sorting

Tags are always sorted alphabetically by title (no manual ordering).

## Out of Scope (Future Work)

- ~~Assigning tags to history items (UI for tagging transactions)~~ → Implemented in 2026-01-09-tags-for-history-items
- ~~Tag filter in actions report (button is placeholder only)~~ → Implemented in 2026-01-10-tags-for-actions-report
- Tag statistics/analytics
