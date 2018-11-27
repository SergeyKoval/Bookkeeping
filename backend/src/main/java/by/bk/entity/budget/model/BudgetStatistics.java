package by.bk.entity.budget.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergey Koval
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class BudgetStatistics {
    private Integer year;
    private Integer month;
    private List<BudgetCategory> categories = new ArrayList<>();
    private BudgetCategory category;
}