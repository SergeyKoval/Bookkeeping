import {AlertType} from './AlertType';

export class Alert {
  public constructor(
    public type: AlertType,
    public message: string,
    public title?: string,
    public timeoutSeconds: number = 3
  ) {}

  public get style(): string {
    switch (this.type) {
      case AlertType.SUCCESS: return 'success';
      case AlertType.INFO: return 'info';
      case AlertType.DANGER: return 'danger';
      case AlertType.WARNING: return 'warning';
      default: return '';
    }
  }

  public initAutoClose(callback: Function): void {
    switch (this.type) {
      case AlertType.SUCCESS:
      case AlertType.INFO: setTimeout((() => callback(this)), this.timeoutSeconds * 1000);
    }
  }
}
