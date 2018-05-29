import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material';

import { filter, switchMap, tap } from 'rxjs/operators';
import { Observable } from 'rxjs';

import { ProfileService } from '../../common/service/profile.service';
import { AlertService } from '../../common/service/alert.service';
import { AlertType } from '../../common/model/alert/AlertType';
import { ConfirmDialogService } from '../../common/components/confirm-dialog/confirm-dialog.service';
import { AccountCategoryDialogComponent } from '../account-category-dialog/account-category-dialog.component';

@Component({
  selector: 'bk-categories',
  templateUrl: './categories.component.html',
  styleUrls: ['./categories.component.css']
})
export class CategoriesComponent implements OnInit {
  public loading: boolean = false;
  public profile: Profile;

  public constructor(
    private _profileService: ProfileService,
    private _dialog: MatDialog,
    private _alertService: AlertService,
    private _confirmDialogService: ConfirmDialogService
  ) {}

  public ngOnInit(): void {
    this.profile = this._profileService.authenticatedProfile;
    this.profile.categories.forEach((category: Category) => category.opened = false);
  }

  public addCategory(): void {
    this.openCategoryDialog({
      'type': 'category',
      'editMode': false
    });
  }

  public editCategory(editCategory: Category): void {
    this.openCategoryDialog({
      'type': 'category',
      'editMode': true,
      'title': editCategory.title,
      'icon': editCategory.icon
    });
  }

  private openCategoryDialog(dialogData: {}): void {
    const dialogResult: Observable<boolean> = this._dialog.open(AccountCategoryDialogComponent, {
      width: '550px',
      position: {top: 'top'},
      data: dialogData
    }).afterClosed();
    this.processCategoryDialogResult(dialogResult);
  }

  private processCategoryDialogResult(dialogResult: Observable<boolean>): void {
    dialogResult
      .pipe(
        filter((result: boolean) => result === true),
        tap((result: boolean) => {
          this._alertService.addAlert(AlertType.SUCCESS, 'Операция успешно выполнена');
          this.loading = true;
        }),
        switchMap(() =>this._profileService.reloadCategoriesInProfile())
      ).subscribe(() => {
      this.loading = false;
    });
  }

















  public hasSubcategories(category: Category): boolean {
    return category.subCategories && category.subCategories.length > 0;
  }



  public deleteCategory(deleteCategory: Category): void {
    const dialogResult: Observable<boolean> = this._confirmDialogService
      .openConfirmDialog('Подтверждение', 'При удалении категории все ее подкатегории а так же существующие операции с этими подкатегориями будут удалены. Продолжить?')
      .afterClosed();
    this.processCategoryDialogResult(dialogResult);
  }

  public moveCategoryUp(category: Category): void {

  }

  public moveCategoryDown(category: Category): void {

  }

  public addSubCategory(category: Category): void {
    this.openCategoryDialog({
      'type': 'subCategory',
      'editMode': false,
      'category': category.title
    });
  }

  public editSubCategory(category: Category, subCategory: SubCategory): void {
    this.openCategoryDialog({
      'type': 'subCategory',
      'editMode': true,
      'category': category.title,
      'subCategory': subCategory.title,
      'subCategoryType': subCategory.type
    });
  }

  public deleteSubCategory(category: Category, subCategory: SubCategory): void {

  }

  public moveSubCategoryUp(category: Category, subCategory: SubCategory): void {

  }

  public moveSubCategoryDown(category: Category, subCategory: SubCategory): void {

  }

  private getCategoryOpened(categories: Category[], title: string): boolean {
    const element: Category = categories.filter((category: Category) => category.title === title)[0];
    return element ? element.opened : false;
  }
}
