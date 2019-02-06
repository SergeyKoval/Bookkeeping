import { CheckboxState } from '../three-state-checkbox/CheckboxState';

export class MultiLevelDropdownItem {
  public constructor(
    public title: string,
    public state?: CheckboxState,
    public icon?: string,
    public children?: MultiLevelDropdownItem[],
    public alias?: string
  ) {}

  public getAlias(): string {
    return this.alias || this.title;
  }
}
