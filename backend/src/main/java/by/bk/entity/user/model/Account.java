package by.bk.entity.user.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergey Koval
 */
@Getter
@Setter
public class Account implements Orderable, Selectable {
    private int order;
    private boolean opened;
    private String title;
    private List<SubAccount> subAccounts;

    public Account() {
    }

    public Account(String title, int order) {
        this.title = title;
        this.order = order;
        this.opened = false;
        this.subAccounts = new ArrayList<>();
    }
}