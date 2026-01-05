package by.bk.entity.user.model;

import by.bk.entity.user.UserPermission;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sergey Koval
 */
@Document(collection = "users")
@Getter
@Setter
public class User {
    @Id
    private String email;
    @JsonIgnore
    private String password;
    private boolean enabled;
    private String code;
    private List<UserPermission> roles;
    private List<UserCurrency> currencies;
    private List<Category> categories;
    private List<Account> accounts;
    private Map<String, Device> devices = new HashMap<>();
    private List<Tag> tags = new ArrayList<>();

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
