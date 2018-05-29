package by.bk.entity.user.model;

import lombok.Getter;

/**
 * @author Sergey Koval
 */
@Getter
public class SubCategory {
    private String title;
    private int order;
    private SubCategoryType type;

    public SubCategory() {
    }

    public SubCategory(String title, int order, SubCategoryType type) {
        this.title = title;
        this.order = order;
        this.type = type;
    }
}