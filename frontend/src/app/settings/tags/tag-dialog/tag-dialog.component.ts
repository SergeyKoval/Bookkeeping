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
  public textColor: string = '#FFFFFF';
  public errorMessage: string;
  public loading: boolean = false;

  private originalTitle: string;

  public presetColors: string[] = [
    '#F44336', '#E91E63', '#9C27B0', '#673AB7',
    '#3F51B5', '#2196F3', '#03A9F4', '#00BCD4',
    '#009688', '#4CAF50', '#8BC34A', '#CDDC39',
    '#FFEB3B', '#FFC107', '#FF9800', '#FF5722'
  ];

  public textColors: string[] = ['#FFFFFF', '#000000'];

  public constructor(
    @Inject(MAT_DIALOG_DATA) public data: {editMode: boolean, tag: Tag},
    private _dialogRef: MatDialogRef<TagDialogComponent>,
    private _profileService: ProfileService
  ) { }

  public ngOnInit(): void {
    if (this.data.editMode && this.data.tag) {
      this.title = this.data.tag.title;
      this.color = this.data.tag.color;
      this.textColor = this.data.tag.textColor || '#FFFFFF';
      this.originalTitle = this.data.tag.title;
    }
  }

  public selectColor(color: string): void {
    this.color = color;
  }

  public selectTextColor(textColor: string): void {
    this.textColor = textColor;
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
      this._profileService.editTag(this.originalTitle, this.title.trim(), this.color, this.textColor, this.data.tag.active)
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
      this._profileService.addTag(this.title.trim(), this.color, this.textColor)
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
