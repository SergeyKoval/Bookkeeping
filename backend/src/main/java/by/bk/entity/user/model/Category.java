package by.bk.entity.user.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergey Koval
 */
@Getter
public class Category implements Orderable, Selectable {
    private int order;
    private String icon;
    private String title;
    private List<SubCategory> subCategories;

    public Category() {
    }

    public Category(String title, String icon, int order) {
        this.title = title;
        this.order = order;
        this.icon = icon;
        this.subCategories = new ArrayList<>();
    }
}