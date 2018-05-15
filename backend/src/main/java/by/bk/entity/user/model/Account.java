package by.bk.entity.user.model;

import lombok.Getter;

import java.util.List;

/**
 * @author Sergey Koval
 */
@Getter
public class Account {
    private int order;
    private boolean opened;
    private String title;
    private List<SubAccount> subAccounts;
}