export interface SelectItem {
  title: string;
  icon?: string;
  parent?: SelectItem;
  children?: SelectItem[];
}
