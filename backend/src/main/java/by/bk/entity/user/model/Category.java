package by.bk.entity.user.model;

import lombok.Getter;

import java.util.List;

/**
 * @author Sergey Koval
 */
@Getter
public class Category {
    private int order;
    private String icon;
    private String title;
    private List<SubCategory> subCategories;
}