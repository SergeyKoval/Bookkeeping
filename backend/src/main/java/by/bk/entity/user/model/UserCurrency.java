package by.bk.entity.user.model;

import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author Sergey Koval
 */
@Getter
public class UserCurrency {
    private String name;
    @Field("default")
    private boolean defaultCurrency;
    private String symbol;
    private int order;
}