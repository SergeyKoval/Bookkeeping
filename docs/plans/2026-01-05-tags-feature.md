# Tags Feature Implementation Plan

**Status:** Implemented (2026-01-05)

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add a tags management page under settings to create, edit, delete, and toggle tags that can later be assigned to history items.

**Architecture:** Tags are stored as a list in the User document (MongoDB). Each tag has a UUID, title, color (hex), and active status. Frontend fetches tags via the existing profile endpoint and manages them through new API endpoints.

**Tech Stack:** Spring Boot 3.5, MongoDB, Angular 21, Angular Material

---

## Task 1: Backend - Create Tag Model

**Files:**
- Create: `backend/src/main/java/by/bk/entity/user/model/Tag.java`

**Step 1: Create the Tag model class**

```java
package by.bk.entity.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
    private String id;
    private String title;
    private String color;
    private boolean active = true;

    public Tag(String id, String title, String color) {
        this.id = id;
        this.title = title;
        this.color = color;
        this.active = true;
    }
}
```

**Step 2: Verify compilation**

Run: `cd /Users/skoval/4work/Bookkeeping/backend && mvn compile -q`
Expected: BUILD SUCCESS

---

## Task 2: Backend - Add Tags to User Model

**Files:**
- Modify: `backend/src/main/java/by/bk/entity/user/model/User.java`

**Step 1: Add tags field to User class**

Add import and field after the `devices` field (around line 32):

```java
private List<Tag> tags = new ArrayList<>();
```

**Step 2: Verify compilation**

Run: `cd /Users/skoval/4work/Bookkeeping/backend && mvn compile -q`
Expected: BUILD SUCCESS

---

## Task 3: Backend - Create Tag Request Model

**Files:**
- Create: `backend/src/main/java/by/bk/controller/model/request/TagRequest.java`

**Step 1: Create the request model**

```java
package by.bk.controller.model.request;

import lombok.Getter;

@Getter
public class TagRequest {
    private String id;
    private String title;
    private String color;
    private Boolean active;
}
```

**Step 2: Verify compilation**

Run: `cd /Users/skoval/4work/Bookkeeping/backend && mvn compile -q`
Expected: BUILD SUCCESS

---

## Task 4: Backend - Add Tag Methods to UserAPI Interface

**Files:**
- Modify: `backend/src/main/java/by/bk/entity/user/UserAPI.java`

**Step 1: Add tag method signatures**

Add after line 68 (after `removeDevice` method):

```java
    SimpleResponse addTag(String login, String title, String color);
    SimpleResponse editTag(String login, String id, String title, String color, Boolean active);
    SimpleResponse deleteTag(String login, String id);
```

**Step 2: Verify compilation**

Run: `cd /Users/skoval/4work/Bookkeeping/backend && mvn compile -q`
Expected: FAIL (methods not implemented in UserService yet)

---

## Task 5: Backend - Implement Tag Methods in UserService

**Files:**
- Modify: `backend/src/main/java/by/bk/entity/user/UserService.java`

**Step 1: Add import for UUID**

Add to imports section:

```java
import java.util.UUID;
```

**Step 2: Add addTag method**

Add before the `private <T extends Orderable>` method (around line 808):

```java
    @Override
    public SimpleResponse addTag(String login, String title, String color) {
        User user = userRepository.findById(login).orElse(null);
        if (user == null) {
            return SimpleResponse.fail();
        }

        List<Tag> tags = user.getTags();
        if (tags == null) {
            tags = new ArrayList<>();
        }

        boolean titleExists = tags.stream()
                .anyMatch(tag -> StringUtils.equalsIgnoreCase(tag.getTitle(), title));
        if (titleExists) {
            return SimpleResponse.alreadyExistsFail();
        }

        Tag newTag = new Tag(UUID.randomUUID().toString(), title, color);
        Query query = Query.query(Criteria.where("email").is(login));
        Update update = new Update().addToSet("tags", newTag);
        return updateUser(query, update);
    }

    @Override
    public SimpleResponse editTag(String login, String id, String title, String color, Boolean active) {
        User user = userRepository.findById(login).orElse(null);
        if (user == null) {
            return SimpleResponse.fail();
        }

        List<Tag> tags = user.getTags();
        if (tags == null) {
            return SimpleResponse.fail();
        }

        int tagIndex = -1;
        for (int i = 0; i < tags.size(); i++) {
            if (StringUtils.equals(tags.get(i).getId(), id)) {
                tagIndex = i;
                break;
            }
        }

        if (tagIndex == -1) {
            return SimpleResponse.fail();
        }

        Tag existingTag = tags.get(tagIndex);

        // Check if new title conflicts with another tag (case-insensitive)
        if (!StringUtils.equalsIgnoreCase(existingTag.getTitle(), title)) {
            boolean titleExists = tags.stream()
                    .anyMatch(tag -> !StringUtils.equals(tag.getId(), id) && StringUtils.equalsIgnoreCase(tag.getTitle(), title));
            if (titleExists) {
                return SimpleResponse.alreadyExistsFail();
            }
        }

        Query query = Query.query(Criteria.where("email").is(login));
        Update update = new Update()
                .set(StringUtils.join("tags.", tagIndex, ".title"), title)
                .set(StringUtils.join("tags.", tagIndex, ".color"), color)
                .set(StringUtils.join("tags.", tagIndex, ".active"), active);
        return updateUser(query, update);
    }

    @Override
    public SimpleResponse deleteTag(String login, String id) {
        Query query = Query.query(Criteria.where("email").is(login));
        Update update = new Update().pull("tags", Collections.singletonMap("id", id));
        return updateUser(query, update);
    }
```

**Step 3: Verify compilation**

Run: `cd /Users/skoval/4work/Bookkeeping/backend && mvn compile -q`
Expected: BUILD SUCCESS

---

## Task 6: Backend - Add Tag Endpoints to ProfileController

**Files:**
- Modify: `backend/src/main/java/by/bk/controller/ProfileController.java`

**Step 1: Add import for TagRequest**

Add to imports:

```java
import by.bk.controller.model.request.TagRequest;
```

**Step 2: Add tag endpoints**

Add after the `removeDevice` method (after line 202):

```java
    @PostMapping("/tag")
    public SimpleResponse addTag(@RequestBody TagRequest request, Principal principal) {
        return userAPI.addTag(principal.getName(), request.getTitle(), request.getColor());
    }

    @PutMapping("/tag")
    public SimpleResponse editTag(@RequestBody TagRequest request, Principal principal) {
        return userAPI.editTag(principal.getName(), request.getId(), request.getTitle(), request.getColor(), request.getActive());
    }

    @DeleteMapping("/tag/{id}")
    public SimpleResponse deleteTag(Principal principal, @PathVariable("id") String id) {
        return userAPI.deleteTag(principal.getName(), id);
    }
```

**Step 3: Verify compilation**

Run: `cd /Users/skoval/4work/Bookkeeping/backend && mvn compile -q`
Expected: BUILD SUCCESS

---

## Task 7: Frontend - Create Tag Model

**Files:**
- Create: `frontend/src/app/common/model/tag.ts`

**Step 1: Create the Tag interface**

```typescript
export interface Tag {
  id: string;
  title: string;
  color: string;
  active: boolean;
}
```

---

## Task 8: Frontend - Update Profile Model

**Files:**
- Modify: `frontend/src/app/common/model/profile.ts`

**Step 1: Add Tag import**

Add after line 4:

```typescript
import { Tag } from './tag';
```

**Step 2: Add tags field to Profile interface**

Add after the `devices` line (line 13):

```typescript
  tags: Tag[];
```

---

## Task 9: Frontend - Add Tag Service Methods

**Files:**
- Modify: `frontend/src/app/common/service/profile.service.ts`

**Step 1: Add import for Tag**

Add to imports (around line 17):

```typescript
import { Tag } from '../model/tag';
```

**Step 2: Add reloadTagsInProfile method**

Add after `reloadDevicesInProfile` method (around line 141):

```typescript
  public reloadTagsInProfile(): Observable<Profile> {
    return this.getUserProfile().pipe(tap(profile => {
      this.authenticatedProfile.tags = profile.tags || [];
    }));
  }
```

**Step 3: Add tag CRUD methods**

Add after the `removeDevice` method (around line 426):

```typescript
  public addTag(title: string, color: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/tag', {title, color});
  }

  public editTag(id: string, title: string, color: string, active: boolean): Observable<SimpleResponse> {
    return this._http.put<SimpleResponse>('/api/profile/tag', {id, title, color, active});
  }

  public deleteTag(id: string): Observable<SimpleResponse> {
    return this._http.delete<SimpleResponse>(`/api/profile/tag/${id}`);
  }
```

---

## Task 10: Frontend - Create Tag Dialog Component

**Files:**
- Create: `frontend/src/app/settings/tags/tag-dialog/tag-dialog.component.ts`
- Create: `frontend/src/app/settings/tags/tag-dialog/tag-dialog.component.html`
- Create: `frontend/src/app/settings/tags/tag-dialog/tag-dialog.component.css`

**Step 1: Create tag-dialog.component.ts**

```typescript
import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { ProfileService } from '../../../common/service/profile.service';
import { Tag } from '../../../common/model/tag';

@Component({
    selector: 'bk-tag-dialog',
    templateUrl: './tag-dialog.component.html',
    styleUrls: ['./tag-dialog.component.css'],
    standalone: false
})
export class TagDialogComponent implements OnInit {
  public title: string = '';
  public color: string = '#4CAF50';
  public errorMessage: string;
  public loading: boolean = false;

  public presetColors: string[] = [
    '#F44336', '#E91E63', '#9C27B0', '#673AB7',
    '#3F51B5', '#2196F3', '#03A9F4', '#00BCD4',
    '#009688', '#4CAF50', '#8BC34A', '#CDDC39',
    '#FFEB3B', '#FFC107', '#FF9800', '#FF5722'
  ];

  public constructor(
    @Inject(MAT_DIALOG_DATA) public data: {editMode: boolean, tag: Tag},
    private _dialogRef: MatDialogRef<TagDialogComponent>,
    private _profileService: ProfileService
  ) { }

  public ngOnInit(): void {
    if (this.data.editMode && this.data.tag) {
      this.title = this.data.tag.title;
      this.color = this.data.tag.color;
    }
  }

  public selectColor(color: string): void {
    this.color = color;
  }

  public save(): void {
    this.errorMessage = null;

    if (!this.title || this.title.trim() === '') {
      this.errorMessage = 'Название обязательно для заполнения';
      return;
    }

    if (!this.color || !/^#[0-9A-Fa-f]{6}$/.test(this.color)) {
      this.errorMessage = 'Некорректный формат цвета';
      return;
    }

    this.loading = true;

    if (this.data.editMode) {
      this._profileService.editTag(this.data.tag.id, this.title.trim(), this.color, this.data.tag.active)
        .subscribe(result => {
          this.loading = false;
          if (result.status === 'FAIL') {
            this.errorMessage = result.message === 'ALREADY_EXIST'
              ? 'Тэг с таким названием уже существует'
              : 'Ошибка при изменении тэга';
            return;
          }
          this._dialogRef.close(true);
        });
    } else {
      this._profileService.addTag(this.title.trim(), this.color)
        .subscribe(result => {
          this.loading = false;
          if (result.status === 'FAIL') {
            this.errorMessage = result.message === 'ALREADY_EXIST'
              ? 'Тэг с таким названием уже существует'
              : 'Ошибка при добавлении тэга';
            return;
          }
          this._dialogRef.close(true);
        });
    }
  }

  public close(): void {
    this._dialogRef.close(false);
  }
}
```

**Step 2: Create tag-dialog.component.html**

```html
<h2 mat-dialog-title align="center" bkDraggable dragTarget=".mat-dialog-container">
  @if (data.editMode) {
    <span>Редактирование тэга</span>
  } @else {
    <span>Новый тэг</span>
  }
</h2>
<mat-dialog-content id="tag-dialog">
  @if (errorMessage) {
    <div class="alert alert-dismissable fade in alert-danger error">
      <strong>{{errorMessage}}</strong>
    </div>
  }
  <div class="table">
    <div class="row title">
      <div class="col-sm-4 left-column">Название:</div>
      <div class="col-sm-8">
        <input type="text" [(ngModel)]="title" class="form-control" placeholder="Введите название">
      </div>
    </div>
    <div class="row">
      <div class="col-sm-4 left-column">Цвет:</div>
      <div class="col-sm-8 color-picker">
        <div class="preset-colors">
          @for (presetColor of presetColors; track presetColor) {
            <span
              class="color-swatch"
              [style.background-color]="presetColor"
              [class.selected]="presetColor === color"
              (click)="selectColor(presetColor)">
            </span>
          }
        </div>
        <div class="custom-color">
          <label>Свой цвет:</label>
          <input type="text" [(ngModel)]="color" class="form-control" placeholder="#RRGGBB">
          <span class="color-preview" [style.background-color]="color"></span>
        </div>
      </div>
    </div>
  </div>
</mat-dialog-content>
<mat-dialog-actions>
  <span class="dialog-actions">
    <button class="btn btn-default action-item" (click)="close()">Закрыть</button>
    <span class="action-item">&nbsp;</span>
    <button class="btn btn-default action-item" [disabled]="loading" (click)="save()">
      @if (!loading) {
        <span>Сохранить</span>
      }
      <bk-spinner [display]="loading" [size]="20"></bk-spinner>
    </button>
  </span>
</mat-dialog-actions>
```

**Step 3: Create tag-dialog.component.css**

```css
#tag-dialog {
  min-width: 400px;
}

.table {
  display: table;
  width: 100%;
}

.row {
  display: table-row;
}

.row > div {
  display: table-cell;
  padding: 8px 4px;
  vertical-align: middle;
}

.left-column {
  width: 100px;
  text-align: right;
  padding-right: 15px;
}

.color-picker {
  padding: 10px 0;
}

.preset-colors {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 15px;
}

.color-swatch {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.2s;
}

.color-swatch:hover {
  border-color: #666;
}

.color-swatch.selected {
  border-color: #333;
  box-shadow: 0 0 4px rgba(0,0,0,0.3);
}

.custom-color {
  display: flex;
  align-items: center;
  gap: 10px;
}

.custom-color label {
  margin: 0;
  white-space: nowrap;
}

.custom-color input {
  width: 100px;
}

.color-preview {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  border: 1px solid #ccc;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  width: 100%;
  padding: 10px;
}

.action-item {
  margin-left: 10px;
}

.error {
  margin-bottom: 15px;
}
```

---

## Task 11: Frontend - Create Tags Component

**Files:**
- Create: `frontend/src/app/settings/tags/tags.component.ts`
- Create: `frontend/src/app/settings/tags/tags.component.html`
- Create: `frontend/src/app/settings/tags/tags.component.css`

**Step 1: Create tags.component.ts**

```typescript
import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';

import { filter } from 'rxjs/operators';

import { ProfileService } from '../../common/service/profile.service';
import { AlertService } from '../../common/service/alert.service';
import { AlertType } from '../../common/model/alert/AlertType';
import { LoadingService } from '../../common/service/loading.service';
import { LoadingDialogComponent } from '../../common/components/loading-dialog/loading-dialog.component';
import { ConfirmDialogService } from '../../common/components/confirm-dialog/confirm-dialog.service';
import { Tag } from '../../common/model/tag';
import { TagDialogComponent } from './tag-dialog/tag-dialog.component';

@Component({
    selector: 'bk-tags',
    templateUrl: './tags.component.html',
    styleUrls: ['./tags.component.css'],
    standalone: false
})
export class TagsComponent implements OnInit {
  public tags: Tag[] = [];

  public constructor(
    private _profileService: ProfileService,
    private _dialog: MatDialog,
    private _loadingService: LoadingService,
    private _alertService: AlertService,
    private _confirmDialogService: ConfirmDialogService
  ) { }

  public ngOnInit(): void {
    this.loadTags();
  }

  private loadTags(): void {
    const profileTags = this._profileService.authenticatedProfile.tags || [];
    this.tags = [...profileTags].sort((a, b) => a.title.localeCompare(b.title, 'ru'));
  }

  public openAddDialog(): void {
    this._dialog.open(TagDialogComponent, {
      width: '500px',
      position: {top: 'top'},
      data: {editMode: false, tag: null}
    })
      .afterClosed()
      .pipe(filter((result: boolean) => result === true))
      .subscribe(() => {
        const loadingDialog: MatDialogRef<LoadingDialogComponent> = this._loadingService.openLoadingDialog('Загрузка...');
        this._profileService.reloadTagsInProfile().subscribe(() => {
          this.loadTags();
          loadingDialog.close();
          this._alertService.addAlert(AlertType.SUCCESS, 'Тэг добавлен');
        });
      });
  }

  public openEditDialog(tag: Tag): void {
    this._dialog.open(TagDialogComponent, {
      width: '500px',
      position: {top: 'top'},
      data: {editMode: true, tag: {...tag}}
    })
      .afterClosed()
      .pipe(filter((result: boolean) => result === true))
      .subscribe(() => {
        const loadingDialog: MatDialogRef<LoadingDialogComponent> = this._loadingService.openLoadingDialog('Загрузка...');
        this._profileService.reloadTagsInProfile().subscribe(() => {
          this.loadTags();
          loadingDialog.close();
          this._alertService.addAlert(AlertType.SUCCESS, 'Тэг изменен');
        });
      });
  }

  public confirmDelete(tag: Tag): void {
    this._confirmDialogService.openConfirmDialog('Подтверждение действия', `Удалить тэг "${tag.title}"?`)
      .afterClosed()
      .pipe(filter((result: boolean) => result === true))
      .subscribe(() => {
        const loadingDialog: MatDialogRef<LoadingDialogComponent> = this._loadingService.openLoadingDialog('Удаление тэга...');
        this._profileService.deleteTag(tag.id).subscribe(response => {
          this._profileService.reloadTagsInProfile().subscribe(() => {
            this.loadTags();
            loadingDialog.close();
            if (response.status === 'SUCCESS') {
              this._alertService.addAlert(AlertType.SUCCESS, 'Тэг удален');
            } else {
              this._alertService.addAlert(AlertType.WARNING, 'Ошибка при удалении тэга');
            }
          });
        });
      });
  }

  public toggleActive(tag: Tag): void {
    const loadingDialog: MatDialogRef<LoadingDialogComponent> = this._loadingService.openLoadingDialog('Сохранение...');
    this._profileService.editTag(tag.id, tag.title, tag.color, !tag.active).subscribe(response => {
      this._profileService.reloadTagsInProfile().subscribe(() => {
        this.loadTags();
        loadingDialog.close();
        if (response.status === 'SUCCESS') {
          this._alertService.addAlert(AlertType.SUCCESS, tag.active ? 'Тэг деактивирован' : 'Тэг активирован');
        } else {
          this._alertService.addAlert(AlertType.WARNING, 'Ошибка при изменении статуса тэга');
        }
      });
    });
  }

  public openReport(tag: Tag): void {
    // Note: Originally a placeholder, implemented in 2026-01-10-tags-for-actions-report
    // Actual implementation navigates to actions report with tag query param:
    // this._router.navigate(['/reports/actions'], { queryParams: { tag: tag.title } });
    this._alertService.addAlert(AlertType.INFO, 'Функция будет доступна позже');
  }
}
```

**Step 2: Create tags.component.html**

```html
<div class="tags-container">
  <div class="header-row">
    <span class="page-title">Тэги</span>
    <button class="btn btn-primary add-btn" (click)="openAddDialog()">
      <span class="glyphicon glyphicon-plus"></span> Добавить тэг
    </button>
  </div>

  @if (tags.length === 0) {
    <div class="empty-state">
      <p>Тэги не созданы</p>
    </div>
  }

  @if (tags.length > 0) {
    <div class="table tags-table">
      <div class="row header">
        <div class="col-color">Цвет</div>
        <div class="col-title">Название</div>
        <div class="col-actions">Действия</div>
      </div>

      @for (tag of tags; track tag.id; let last = $last) {
        <div class="row tag-row" [class.inactive]="!tag.active">
          <div class="col-color">
            <span class="color-chip" [style.background-color]="tag.color"></span>
          </div>
          <div class="col-title">
            <span class="tag-title">{{tag.title}}</span>
            @if (!tag.active) {
              <span class="inactive-badge">(неактивен)</span>
            }
          </div>
          <div class="col-actions">
            <bk-popover glyphiconClass="stats" text="Отчет" placement="left" (click)="openReport(tag)"></bk-popover>
            <bk-popover glyphiconClass="pencil" text="Редактировать" placement="left" (click)="openEditDialog(tag)"></bk-popover>
            <bk-popover glyphiconClass="remove" text="Удалить" placement="left" (click)="confirmDelete(tag)"></bk-popover>
            <mat-slide-toggle
              color="primary"
              [checked]="tag.active"
              (change)="toggleActive(tag)"
              matTooltip="{{tag.active ? 'Деактивировать' : 'Активировать'}}">
            </mat-slide-toggle>
          </div>
        </div>
        @if (!last) {
          <hr class="tag-divider"/>
        }
      }
    </div>
  }
</div>
```

**Step 3: Create tags.component.css**

```css
.tags-container {
  padding: 15px;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 18px;
  font-weight: 500;
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 5px;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #666;
}

.tags-table {
  display: table;
  width: 100%;
}

.row {
  display: table-row;
}

.row > div {
  display: table-cell;
  padding: 12px 8px;
  vertical-align: middle;
}

.header {
  font-weight: 600;
  background-color: #f5f5f5;
}

.header > div {
  padding: 10px 8px;
}

.col-color {
  width: 60px;
  text-align: center;
}

.col-title {
  width: auto;
}

.col-actions {
  width: 180px;
  text-align: right;
  white-space: nowrap;
}

.color-chip {
  display: inline-block;
  width: 24px;
  height: 24px;
  border-radius: 4px;
  border: 1px solid rgba(0,0,0,0.1);
}

.tag-title {
  font-size: 14px;
}

.inactive-badge {
  color: #999;
  font-size: 12px;
  margin-left: 8px;
}

.tag-row.inactive {
  opacity: 0.6;
}

.tag-row.inactive .color-chip {
  filter: grayscale(100%);
}

.tag-divider {
  margin: 0;
  border-color: #eee;
}

mat-slide-toggle {
  margin-left: 10px;
}
```

---

## Task 12: Frontend - Add Route for Tags

**Files:**
- Modify: `frontend/src/app/routes.ts`

**Step 1: Add import for TagsComponent**

Add after line 17:

```typescript
import { TagsComponent } from './settings/tags/tags.component';
```

**Step 2: Add tags route**

Add after the devices route (after line 107):

```typescript
      {
        path: 'tags',
        component: TagsComponent,
        data: {title: 'Бухгалтерия - настройки - тэги'}
      }
```

---

## Task 13: Frontend - Add Tags Tab to Settings Navigation

**Files:**
- Modify: `frontend/src/app/settings/settings.component.html`

**Step 1: Add tags tab**

Add after line 6 (after Девайсы tab):

```html
  <li role="presentation" routerLinkActive="active"><a [routerLink]="['/settings/tags']">Тэги</a></li>
```

---

## Task 14: Frontend - Register Components in Module

**Files:**
- Modify: `frontend/src/app/bk.module.ts`

**Step 1: Add imports for new components**

Add after line 93:

```typescript
import { TagsComponent } from './settings/tags/tags.component';
import { TagDialogComponent } from './settings/tags/tag-dialog/tag-dialog.component';
```

**Step 2: Add components to declarations**

Add after `DeviceMessageAssignDialogComponent` (after line 178):

```typescript
    TagsComponent,
    TagDialogComponent,
```

---

## Task 15: Verify Frontend Compilation

**Step 1: Build frontend**

Run: `cd /Users/skoval/4work/Bookkeeping/frontend && npm run build`
Expected: Build successful with no errors

---

## Task 16: Manual Testing

**Step 1: Start backend**

Run: `cd /Users/skoval/4work/Bookkeeping/backend && mvn spring-boot:run`

**Step 2: Start frontend (in separate terminal)**

Run: `cd /Users/skoval/4work/Bookkeeping/frontend && npm start`

**Step 3: Test in browser**

1. Navigate to http://localhost:4200
2. Login with test credentials
3. Go to Settings > Тэги
4. Test adding a new tag with title and color
5. Test editing a tag
6. Test toggling active/inactive
7. Test deleting a tag
8. Verify tags are sorted alphabetically
9. Verify inactive tags show grayed out

---

## Summary

This plan creates the complete tags management feature:

**Backend (6 tasks):**
- Tag model with id, title, color, active fields
- User model extended with tags list
- TagRequest DTO
- UserAPI interface methods
- UserService implementation with MongoDB operations
- ProfileController REST endpoints

**Frontend (8 tasks):**
- Tag model interface
- Profile model update
- ProfileService CRUD methods
- TagDialogComponent for add/edit with color picker
- TagsComponent for list view with actions
- Route configuration
- Settings navigation tab
- Module registration
