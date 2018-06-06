package by.bk.entity.history;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author Sergey Koval
 */
@Document(collection = "history")
@Getter
@Setter
public class HistoryItem {
    @Id
    private String id;
    @JsonIgnore
    private String user;
    private int year;
    private int month;
    private int day;
    private int order;
    private HistoryType type;
    private String category;
    private String subCategory;
    private String description;
    private Balance balance;
}