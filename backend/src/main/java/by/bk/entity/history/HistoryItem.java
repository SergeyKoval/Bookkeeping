package by.bk.entity.history;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.List;

/**
 * @author Sergey Koval
 */
@Document(collection = "history")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HistoryItem {
    @Id
    private String id;
    @JsonIgnore
    private String user;
    private int year;
    private int month;
    private int day;
    private Integer order;
    private HistoryType type;
    private String category;
    private String subCategory;
    private String description;
    private Balance balance;
    private boolean archived;
    private boolean notProcessed;
    private String goal;
    private List<DeviceMessage> deviceMessages;
    private List<DeviceMessage> duplicateMessages;

    @JsonIgnore
    Balance cloneBalance() {
        Balance clonedBalance = new Balance();
        clonedBalance.setValue(balance.getValue());
        clonedBalance.setNewValue(balance.getNewValue());
        clonedBalance.setAccount(balance.getAccount());
        clonedBalance.setAccountTo(balance.getAccountTo());
        clonedBalance.setSubAccount(balance.getSubAccount());
        clonedBalance.setSubAccountTo(balance.getSubAccountTo());
        clonedBalance.setCurrency(balance.getCurrency());
        clonedBalance.setNewCurrency(balance.getNewCurrency());
        if (balance.getAlternativeCurrency() != null) {
            clonedBalance.setAlternativeCurrency(new HashMap<>(balance.getAlternativeCurrency()));
        }

        return clonedBalance;
    }
}
