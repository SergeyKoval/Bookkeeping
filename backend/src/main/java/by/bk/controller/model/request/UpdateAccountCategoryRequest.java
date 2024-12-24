package by.bk.controller.model.request;

import by.bk.entity.currency.Currency;
import by.bk.entity.user.model.SubCategoryType;
import lombok.Getter;

import java.util.Map;

/**
 * @author Sergey Koval
 */
@Getter
public class UpdateAccountCategoryRequest {
    private String title;
    private String oldTitle;
    private Direction direction;
    private String parentTitle;
    private String icon;
    private Map<Currency, Double> balance;
    private boolean toggleState;
    private SubCategoryType subCategoryType;
    private Boolean excludeFromTotals;
}
