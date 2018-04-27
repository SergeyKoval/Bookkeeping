import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';

import { ProfileService } from '../../../../common/service/profile.service';

@Component({
  selector: 'bk-category-dialog',
  templateUrl: './category-dialog.component.html',
  styleUrls: ['./category-dialog.component.css']
})
export class CategoryDialogComponent implements OnInit {
  public categoryIcons: string[] = ['avto.gif', 'deti.gif', 'home.gif'];
  public title: string;
  public errorMessage: string;

  public constructor (
    @Inject(MAT_DIALOG_DATA) public data: {editMode: boolean, type: string, category: string, subCategory: string, icon: string, subCategoryType: string},
    private _dialogRef: MatDialogRef<CategoryDialogComponent>,
    private _profileService: ProfileService
  ) {}

  public ngOnInit(): void {
    if (this.data.editMode) {
      this.title = this.isCategory() ? this.data.category : this.data.subCategory;
    }
    if (!this.data.editMode && this.isCategory()) {
      this.data.icon = this.categoryIcons[0];
    }
  }

  public chooseIcon(icon: string): void {
    this.data.icon = icon;
  }

  public save(): void {
    if (this.validateBeforeSave()) {
      this._dialogRef.close(true);
    }
  }

  public close(dialogResult: boolean): void {
    this._dialogRef.close(dialogResult);
  }

  public isCategory(): boolean {
    return this.data.type === 'category';
  }

  public changeType(type: string): void {
    this.data.subCategoryType = type;
  }

  private validateBeforeSave(): boolean {
    this.errorMessage = null;
    if (!this.title || this.title.trim().length === 0) {
      this.errorMessage = 'Название обязательно для заполнения';
      return false;
    }

    if (!this.isCategory() && !this.data.subCategoryType) {
      this.errorMessage = 'Тип обязателен для заполнения';
      return false;
    }

    if (!this.isCategory() && !this.data.editMode && this._profileService.authenticatedProfile.categories
      .filter((category: Category) => category.title === this.data.category)[0]
      .subCategories.filter((subCategory: SubCategory) => subCategory.title === this.title).length !== 0) {
      this.errorMessage = 'Подкатегория с таким названием уже существует';
      return false;
    }

    if (this.isCategory() && !this.data.editMode && this._profileService.authenticatedProfile.categories.filter((category: Category) => category.title === this.title).length !== 0) {
        this.errorMessage = 'Категория с таким названием уже существует';
        return false;
    }

    if (!this.isCategory() && this.data.editMode && this._profileService.authenticatedProfile.categories
      .filter((category: Category) => category.title === this.data.category)[0]
      .subCategories.filter((subCategory: SubCategory) => subCategory.title === this.title && subCategory.type === this.data.subCategoryType).length !== 0) {
      this.errorMessage = 'Подкатегория с таким названием уже существует';
      return false;
    }

    if (this.isCategory() && this.data.category !== this.title && this._profileService.authenticatedProfile.categories.filter((category: Category) => category.title === this.title).length !== 0) {
      this.errorMessage = 'Категория с таким названием уже существует';
      return false;
    }

    return true;
  }
}
