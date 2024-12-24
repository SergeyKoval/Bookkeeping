package by.bk.entity.user.model;

import by.bk.entity.currency.Currency;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sergey Koval
 */
@Getter
@Setter
public class SubAccount implements Orderable, Selectable {
    private String title;
    private int order;
    private String icon;
    private Map<Currency, Double> balance;
    private Map<String, List<DeviceAssociation>> device = new HashMap<>();
    private Boolean excludeFromTotals;

    public SubAccount() {
    }

    public SubAccount(String title, int order, String icon, Map<Currency, Double> balance, Boolean excludeFromTotals) {
        this.title = title;
        this.order = order;
        this.icon = icon;
        this.balance = balance;
        this.excludeFromTotals = excludeFromTotals;
    }
}
