import { createEntityAdapter, EntityAdapter, EntityState } from '@ngrx/entity';
import { createReducer, on } from '@ngrx/store';

import { LoginPageActions, SummaryActions, UserActions } from '../../actions';

export const ACCOUNTS_FEATURE_KEY = 'accounts';

export interface Account {
  ownerId: number;
  title: string;
  order: number;
  opened: boolean;
  settingsOpened: boolean;
  subAccounts: Array<SubAccount>;
}

export interface SubAccount {
  title: string;
  order: number;
  icon: string;
  balance: {[currency: string]: number};
}

export interface State extends EntityState<Account> {
  accountIcons: {};
}

export const ADAPTER: EntityAdapter<Account> = createEntityAdapter<Account>({
  selectId: account => account.title,
  sortComparer: (first, second) => first.order - second.order
});

export const INITIAL_STATE: State = ADAPTER.getInitialState({
  accountIcons: {}
});

export const REDUCER = createReducer(
  INITIAL_STATE,
  on(LoginPageActions.LOGIN_REDIRECT, () => ({
    ...INITIAL_STATE
  })),
  on(SummaryActions.TOGGLE_ACCOUNT, (state, { account, opened }) => {
    return ADAPTER.updateOne({
      id: account,
      changes: { opened }
    }, state);
  }),
  on(UserActions.LOAD_PROFILE_FINISHED, (state, { profile }) => {
    const accountIcons = {};
    profile.accounts.forEach((account: Account) => account.subAccounts.forEach((subAccount: SubAccount) => accountIcons[`${account.title}-${subAccount.title}`] = subAccount.icon));
    return ADAPTER.setAll(profile.accounts, { ...state, accountIcons });
  })
);

const {
  selectAll,
} = ADAPTER.getSelectors();

export const selectAllAccounts = selectAll;
