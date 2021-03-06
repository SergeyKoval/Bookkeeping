package by.bk.entity.budget.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Sergey Koval
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BalanceValue {
    private Double value;
    private Double completeValue;

    public static BalanceValue initValue(Double value) {
        return new BalanceValue(value, 0d);
    }

    public BalanceValue initCompleteValue(Double completeValue) {
        return new BalanceValue(0d, completeValue);
    }
}