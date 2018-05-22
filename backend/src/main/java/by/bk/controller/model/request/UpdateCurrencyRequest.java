package by.bk.controller.model.request;

import by.bk.entity.currency.Currency;
import lombok.Getter;

/**
 * @author Sergey Koval
 */
@Getter
public class UpdateCurrencyRequest {
    private Currency name;
    private Boolean use;
    private Direction direction;
}