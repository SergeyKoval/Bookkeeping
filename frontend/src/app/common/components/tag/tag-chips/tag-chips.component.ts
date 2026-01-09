import { Component, Input } from '@angular/core';

import { ProfileService } from '../../../service/profile.service';
import { Tag } from '../../../model/tag';

@Component({
    selector: 'bk-tag-chips',
    templateUrl: './tag-chips.component.html',
    styleUrls: ['./tag-chips.component.css'],
    standalone: false
})
export class TagChipsComponent {
  @Input() public tagTitles: string[] = [];

  public constructor(private _profileService: ProfileService) {}

  public getTag(title: string): Tag | undefined {
    const profileTags = this._profileService.authenticatedProfile?.tags || [];
    return profileTags.find(tag => tag.title === title);
  }
}
