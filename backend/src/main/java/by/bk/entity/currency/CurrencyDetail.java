package by.bk.entity.currency;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

/**
 * @author Sergey Koval
 */
@Document(collection = "currencyDetails")
@Getter
@Setter
public class CurrencyDetail {
    @Id
    @JsonIgnore
    private String id;
    private Currency name;
    private Integer year;
    private Integer month;
    private Integer day;
    private Map<Currency, Double> conversions;
}