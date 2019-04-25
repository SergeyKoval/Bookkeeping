import { IMyDateRangeModel, IMyDrpOptions } from 'mydaterangepicker';

import { ProfileService } from '../common/service/profile.service';
import { MultiLevelDropdownItem } from '../common/components/multi-level-dropdown/MultiLevelDropdownItem';
import { CheckboxState } from '../common/components/three-state-checkbox/CheckboxState';
import { AssetImagePipe } from '../common/pipes/asset-image.pipe';
import { DateUtils } from '../common/utils/date-utils';

export abstract class BaseReport {
  public periodFilter: IMyDateRangeModel;
  public datePickerOptions: IMyDrpOptions = {
    dateFormat: 'dd.mm.yyyy',
    inline: false,
    dayLabels: DateUtils.DAY_LABELS,
    monthLabels: DateUtils.MONTH_LABELS,
    selectBeginDateTxt: 'Выберите начало периода',
    selectEndDateTxt: 'Выберите конец периода',
    width: '225px',
    height: '32px',
    selectorWidth: '225px',
    disableSince: {year: new Date().getFullYear(), month: new Date().getMonth() + 1, day: new Date().getDate() + 1}
  };

  protected constructor (
    protected _profileService: ProfileService,
    protected _imagePipe: AssetImagePipe,
  ) { }

  public onDateRangeChanged(dateRange: IMyDateRangeModel): void {
    this.periodFilter = dateRange;
  }

  protected populateAccountsFilter(accounts: FinAccount[]): MultiLevelDropdownItem[] {
    const accountsFilter: MultiLevelDropdownItem[] = [];
    accounts.forEach(account => {
      const subAccounts: MultiLevelDropdownItem[] = [];
      account.subAccounts.forEach(subAccount => {
        subAccounts.push(new MultiLevelDropdownItem(subAccount.title, CheckboxState.CHECKED, this._imagePipe.transform(subAccount.icon, 'account')));
      });
      accountsFilter.push(new MultiLevelDropdownItem(account.title, CheckboxState.CHECKED, null, subAccounts));
    });

    return accountsFilter;
  }

  protected populateCategoriesFilter(categories: Category[]): MultiLevelDropdownItem[] {
    const categoriesFilter: MultiLevelDropdownItem[] = [];
    const incomeCategories: MultiLevelDropdownItem[] = [];
    const expenseCategories: MultiLevelDropdownItem[] = [];

    categories.forEach((category: Category) => {
      const income: MultiLevelDropdownItem[] = [];
      const expense: MultiLevelDropdownItem[] = [];
      category.subCategories.forEach(subCategory => {
        if (subCategory.type === 'income') {
          income.push(new MultiLevelDropdownItem(subCategory.title, CheckboxState.CHECKED));
        } else {
          expense.push(new MultiLevelDropdownItem(subCategory.title, CheckboxState.CHECKED));
        }
      });

      if (income.length > 0) {
        incomeCategories.push(new MultiLevelDropdownItem(category.title, CheckboxState.CHECKED, this._imagePipe.transform(category.icon, 'category'), income));
      }
      if (expense.length > 0) {
        expenseCategories.push(new MultiLevelDropdownItem(category.title, CheckboxState.CHECKED, this._imagePipe.transform(category.icon, 'category'), expense));
      }
    });

    categoriesFilter.push(new MultiLevelDropdownItem('Доход', CheckboxState.CHECKED, null, incomeCategories, 'income'));
    categoriesFilter.push(new MultiLevelDropdownItem('Расход', CheckboxState.CHECKED, null, expenseCategories, 'expense'));

    return categoriesFilter;
  }
}
