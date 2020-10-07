import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'bk-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SettingsComponent {
}
