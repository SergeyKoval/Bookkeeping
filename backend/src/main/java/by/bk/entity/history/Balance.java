package by.bk.entity.history;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * @author Sergey Koval
 */
@Getter
@Setter
@ToString
public class Balance {
    private Double value;
    private Double newValue;
    private String account;
    private String accountTo;
    private String subAccount;
    private String subAccountTo;
    private String currency;
    private String newCurrency;
    private Map<String, Double> alternativeCurrency;
}