package by.bk.controller.model.request;

import lombok.Getter;

/**
 * @author Sergey Koval
 */
@Getter
public class UpdateAccountCategoryRequest {
    private String title;
    private String oldTitle;
    private Direction direction;
}