package by.bk.controller.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TendencyReportResponse {
    private int year;
    private int month;
    private double income;
    private double expense;
    private double difference;
}
