import { Component, Input } from '@angular/core';
import { UntypedFormControl } from '@angular/forms';

@Component({
    selector: 'bk-authentication-input',
    templateUrl: './input.component.html',
    styleUrls: ['./input.component.css'],
    standalone: false
})
export class InputComponent {
  @Input()
  public input: UntypedFormControl;
  @Input()
  public type: string;
  @Input()
  public submitted: boolean;
  @Input()
  public alwaysEditing: boolean;
  @Input()
  public inputId: string;

  public isInvalidValue(): boolean {
    return this.submitted && this.input.invalid;
  }

  public isEditing(): boolean {
    return this.alwaysEditing || (this.input.dirty && this.input.value.length > 0);
  }
}
