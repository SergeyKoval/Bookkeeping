import { createEntityAdapter, EntityAdapter, EntityState } from '@ngrx/entity';
import { createReducer, on } from '@ngrx/store';

import { LoginPageActions, UserActions } from '../../actions';

export const CATEGORIES_FEATURE_KEY = 'categories';

export interface Category {
  id: number;
  ownerId: number;
  order: number;
  icon: string;
  title: string;
  subCategories: Array<SubCategory>;
  opened: boolean;
}

export interface SubCategory {
  order: number;
  title: string;
  type: string;
}

export interface State extends EntityState<Category> {
  categoryIcons: {};
}

export const ADAPTER: EntityAdapter<Category> = createEntityAdapter<Category>({
  selectId: category => category.title,
  sortComparer: (first, second) => first.order - second.order
});

export const INITIAL_STATE: State = ADAPTER.getInitialState({
  categoryIcons: {}
});

export const REDUCER = createReducer(
  INITIAL_STATE,
  on(LoginPageActions.LOGIN_REDIRECT, () => ({
    ...INITIAL_STATE
  })),
  on(UserActions.LOAD_PROFILE_FINISHED, (state, { profile }) => {
    const categoryIcons = {};
    profile.categories.forEach((category: Category) => categoryIcons[category.title] = category.icon);
    return ADAPTER.setAll(profile.categories, { ...state, categoryIcons });
  })
);
