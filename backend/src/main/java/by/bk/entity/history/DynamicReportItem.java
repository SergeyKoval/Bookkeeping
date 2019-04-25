package by.bk.entity.history;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DynamicReportItem extends SummaryReportItem {
    private int year;
    private int month;
}