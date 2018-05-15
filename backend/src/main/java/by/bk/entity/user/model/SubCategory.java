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
}