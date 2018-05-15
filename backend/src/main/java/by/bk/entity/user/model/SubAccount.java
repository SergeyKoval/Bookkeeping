package by.bk.entity.user.model;

import lombok.Getter;

import java.util.List;

/**
 * @author Sergey Koval
 */
@Getter
public class SubAccount {
    private String title;
    private int order;
    private String icon;
    private List<Balance> balance;
}