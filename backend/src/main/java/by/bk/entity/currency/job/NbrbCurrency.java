package by.bk.entity.currency.job;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Sergey Koval
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NbrbCurrency {
    @JsonProperty("Date")
    private String date;
    @JsonProperty("Cur_OfficialRate")
    private Double officialRate;
}