package by.bk.controller.model.request;

import by.bk.entity.currency.Currency;
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
}