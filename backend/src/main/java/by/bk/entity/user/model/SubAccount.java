package by.bk.entity.user.model;

import by.bk.entity.currency.Currency;
import lombok.Getter;

import java.util.Map;

/**
 * @author Sergey Koval
 */
@Getter
public class SubAccount {
    private String title;
    private int order;
    private String icon;
    private Map<Currency, Double> balance;
}