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
    this.tags = [...profileTags].sort((a, b) => a.title.localeCompare(b.title));
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
        this._profileService.deleteTag(tag.title).subscribe(response => {
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
    this._profileService.editTag(tag.title, tag.title, tag.color, tag.textColor, !tag.active).subscribe(response => {
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
    this._alertService.addAlert(AlertType.INFO, 'Функция будет доступна позже');
  }
}
