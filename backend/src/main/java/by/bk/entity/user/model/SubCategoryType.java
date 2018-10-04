package by.bk.entity.user.model;

import by.bk.entity.history.HistoryType;

/**
 * @author Sergey Koval
 */
public enum SubCategoryType {
    expense {
        @Override
        public HistoryType convertToHistoryType() {
            return HistoryType.expense;
        }
    },
    income {
        @Override
        public HistoryType convertToHistoryType() {
            return HistoryType.income;
        }
    };

    public abstract HistoryType convertToHistoryType();
}