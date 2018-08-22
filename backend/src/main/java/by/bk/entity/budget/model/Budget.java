package by.bk.entity.budget.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author Sergey Koval
 */
@Document(collection = "budget")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Budget {
    @Id
    private String id;
    @JsonIgnore
    private String user;
    private Integer year;
    private Integer month;
    private BudgetDetails expense;
    private BudgetDetails income;

    public Budget(String user, Integer year, Integer month) {
        this.user = user;
        this.year = year;
        this.month = month;
        this.expense = new BudgetDetails(true);
        this.income = new BudgetDetails(true);
    }
}