package by.bk.entity.history;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author Sergey Koval
 */
@Document(collection = "history")
@Getter
@Setter
@ToString
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
}