import { CheckboxState } from '../three-state-checkbox/CheckboxState';

export class MultiLevelDropdownItem {
  public constructor(
    public title: string,
    public state?: CheckboxState,
    public icon?: string,
    public children?: MultiLevelDropdownItem[]
  ) {}
}
