import { Component, Inject, OnInit } from '@angular/core';

import { filter, tap } from 'rxjs/operators';

import { ProfileService } from '../../../common/service/profile.service';
import { LoadingService } from '../../../common/service/loading.service';
import { LoadingDialogComponent } from '../../../common/components/loading-dialog/loading-dialog.component';
import { AlertService } from '../../../common/service/alert.service';
import { AlertType } from '../../../common/model/alert/AlertType';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Category } from '../../../common/model/category';

@Component({
  selector: 'bk-move-sub-category-dialog',
  templateUrl: './move-sub-category-dialog.component.html',
  styleUrls: ['./move-sub-category-dialog.component.css']
})
export class MoveSubCategoryDialogComponent implements OnInit {
  public categoryTitle: string;
  public errors: string;
  public categories: Category[];

  public constructor(
    @Inject(MAT_DIALOG_DATA) public data: {category: string, subCategory: string, categories: Category[], type: string},
    private _dialogRef: MatDialogRef<MoveSubCategoryDialogComponent>,
    private _profileService: ProfileService,
    private _loadingService: LoadingService,
    private _alertService: AlertService
  ) { }

  public ngOnInit(): void {
    this.categories = this.data.categories.filter(category => category.title !== this.data.category);
  }

  public getCategoryIcon(category: string): string {
    return this._profileService.getCategoryIcon(category);
  }

  public changeCategory(category: Category): void {
    this.categoryTitle = category.title;
  }

  public close(refreshPage: boolean): void {
    this._dialogRef.close(refreshPage);
  }

  public save(): void {
    this.errors = null;
    if (!this.categoryTitle) {
      this.errors = 'Новая категория не выбрана';
      return;
    }

    if (this.categories.filter(category => category.title === this.categoryTitle)[0]
      .subCategories.filter(subCategoryItem => subCategoryItem.title === this.data.subCategory && subCategoryItem.type === this.data.type)[0]
    ) {
      this.errors = 'Такая подкатегория уже существует';
      return;
    }

    const loadingDialog: MatDialogRef<LoadingDialogComponent> = this._loadingService.openLoadingDialog('Перемещение категории...');
    this._profileService.moveCategory(this.data.category, this.categoryTitle, this.data.subCategory, this.data.type)
      .pipe(
        tap(response => {
          loadingDialog.close();
          if (response.status !== 'SUCCESS') {
            this._alertService.addAlert(AlertType.WARNING, 'Ошибка при перемещении категории');
          }
        }),
        filter(response => response.status === 'SUCCESS')
      ).subscribe(() => this._dialogRef.close(true));
  }
}
