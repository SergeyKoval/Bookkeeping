package by.bk.controller.model.request;

import lombok.Getter;

@Getter
public class TagRequest {
    private String title;
    private String oldTitle;
    private String color;
    private String textColor;
    private Boolean active;
}
