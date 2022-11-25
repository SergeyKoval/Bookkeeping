package by.bk.entity.currency.job;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlfaBankCurrency {
  private Double rate;
  private String iso;
  private Integer quantity;
}
