import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material';

import { Observable } from 'rxjs/index';
import { filter } from 'rxjs/operators';
import { isNullOrUndefined } from 'util';

import { ProfileService } from '../../common/service/profile.service';
import { BalanceDialogComponent } from '../accounts/balance-dialog/balance-dialog.component';

@Component({
  selector: 'bk-account-category-dialog',
  templateUrl: './account-category-dialog.component.html',
  styleUrls: ['./account-category-dialog.component.css']
})
export class AccountCategoryDialogComponent implements OnInit {
  public accountIcons: string[] = ['payok.gif', 'avtokarta.gif', 'alfa.gif', 'belagro.gif', 'belarusbank.gif', 'belgazprom1.gif', 'parents.gif', 'collega.gif', 'rabotnik.gif', 'kwallet.gif', 'Money.gif', 'other_exp.gif'];
  public categoryIcons: string[] = ['avto.gif', 'deti.gif', 'home.gif'];
  public title: string;
  public errorMessage: string;
  public loading: boolean;

  public constructor(
    @Inject(MAT_DIALOG_DATA) public data: {editMode: boolean, type: string, parentTitle: string, title: string, icon: string, balance: {[currency: string]: number}, subCategoryType: string},
    private _dialogRef: MatDialogRef<AccountCategoryDialogComponent>,
    private _profileService: ProfileService,
    private _dialog: MatDialog
  ) { }

  public ngOnInit(): void {
    if (this.data.editMode) {
      this.title = this.data.title;
    } else {
      if (this.isSubAccountType()) {
        this.data.icon = this.accountIcons[0];
      } else if (this.isCategoryType()) {
        this.data.icon = this.categoryIcons[0];
      }
    }
  }

  public editSubAccountBalance(): void {
    this._dialog.open(BalanceDialogComponent, {
      position: {top: 'top'},
      width: '350px',
      data: {'subAccountBalance': Object.assign({}, this.data.balance)}
    }).afterClosed()
      .pipe(filter((result: {[currency: string]: number}) => result !== null))
      .subscribe((result: {[currency: string]: number}) => {
        this.data.balance = result;
      });
  }

  public save(): void {
    this.loading = true;

    if (isNullOrUndefined(this.title) || this.title === '') {
      this.errorMessage = 'Название обязательно для заполнения';
      this.loading = false;
      return;
    }

    if(this.isAccountType()) {
      if (!this.data.editMode) {
        this.processResult(this._profileService.addAccount(this.title), 'Счет с таким названием уже существует', 'Ошибка при добавлении счета');
      } else if (this.title === this.data.title) {
        this._dialogRef.close(false);
      } else {
        this.processResult(this._profileService.editAccount(this.data.title, this.title), 'Счет с таким названием уже существует', 'Ошибка при изминении счета');
      }
    }

    if(this.isSubAccountType()) {
      if (!this.data.editMode) {
        this.processResult(this._profileService.addSubAccount(this.title, this.data.parentTitle, this.data.icon, this.data.balance), 'Субчет с таким названием уже существует', 'Ошибка при добавлении субсчета');
      } else {
        this.processResult(this._profileService.editSubAccount(this.data.parentTitle, this.data.title, this.title, this.data.icon, this.data.balance), 'Субсчет с таким названием уже существует', 'Ошибка при изменении субсчета');
      }
    }

    if(this.isCategoryType()) {
      if (!this.data.editMode) {
        this.processResult(this._profileService.addCategory(this.title, this.data.icon), 'Категория с таким названием уже существует', 'Ошибка при добавлении категории');
      } else {
        this.processResult(this._profileService.editCategory(this.data.title, this.title, this.data.icon), 'Категория с таким названием уже существует', 'Ошибка при изминении категории');
      }
    }

    if(this.isSubCategoryType()) {
      if (!this.data.editMode) {
        if (isNullOrUndefined(this.data.subCategoryType)) {
          this.errorMessage = 'Необходимо выбрать тип';
          this.loading = false;
          return;
        }
        this.processResult(this._profileService.addSubCategory(this.title, this.data.parentTitle, this.data.subCategoryType), 'Подкатегория с таким названием уже существует', 'Ошибка при добавлении подкатегории');
      } else if (this.title === this.data.title) {
        this._dialogRef.close(false);
      } else {
        this.processResult(this._profileService.editSubCategory(this.data.parentTitle, this.data.title, this.title, this.data.subCategoryType), 'Подкатегория с таким названием уже существует', 'Ошибка при изменении подкатегории');
      }
    }
  }

  public isAccountType(): boolean {
    return this.data.type === 'account';
  }

  public isSubAccountType(): boolean {
    return this.data.type === 'subAccount';
  }

  public isCategoryType(): boolean {
    return this.data.type === 'category';
  }

  public isSubCategoryType(): boolean {
    return this.data.type === 'subCategory';
  }

  public close(dialogResult: boolean): void {
    this._dialogRef.close(dialogResult);
  }

  public chooseIcon(icon: string): void {
    this.data.icon = icon;
  }

  public changeType(type: string): void {
    this.data.subCategoryType = type;
  }

  private processResult(resultObservable: Observable<SimpleResponse>, alreadyExistError: string, otherError: string): void {
    resultObservable.subscribe(result => {
      this.loading = false;
      if (result.status === 'FAIL') {
        this.errorMessage = result.message === 'ALREADY_EXIST' ? alreadyExistError: otherError;
        return;
      }

      this._dialogRef.close(true);
    });
  }
}
