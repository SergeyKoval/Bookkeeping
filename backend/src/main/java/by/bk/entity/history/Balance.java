package by.bk.entity.history;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author Sergey Koval
 */
@Getter
@Setter
public class Balance {
    private Double value;
    private String account;
    private String subAccount;
    private String currency;
    private Map<String, Double> alternativeCurrency;
}