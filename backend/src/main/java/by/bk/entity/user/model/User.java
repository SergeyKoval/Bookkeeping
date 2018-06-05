package by.bk.entity.user.model;

import by.bk.entity.user.UserPermission;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collections;
import java.util.List;

/**
 * @author Sergey Koval
 */
@Document(collection = "users")
@Getter
public class User {
    @Id
    private String email;
    @JsonIgnore
    private String password;
    private List<UserPermission> roles;
    private List<UserCurrency> currencies;
    private List<Category> categories;
    private List<Account> accounts;

    public User() {
    }

    public User(String email, String password, List<UserPermission> roles) {
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.currencies = Collections.emptyList();
        this.categories = Collections.emptyList();
        this.accounts = Collections.emptyList();
    }
}