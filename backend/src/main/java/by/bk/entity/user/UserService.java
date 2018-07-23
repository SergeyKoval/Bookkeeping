package by.bk.entity.user;

import by.bk.controller.model.request.Direction;
import by.bk.controller.model.response.SimpleResponse;
import by.bk.entity.currency.Currency;
import by.bk.entity.history.*;
import by.bk.entity.history.Balance;
import by.bk.entity.user.exception.SelectableItemMissedSettingUpdateException;
import by.bk.entity.user.model.*;
import by.bk.security.model.JwtUser;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Sergey Koval
 */
@Service
public class UserService implements UserAPI, UserDetailsService {
    private static final Log LOG = LogFactory.getLog(UserService.class);
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("##0.00");

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public SimpleResponse updateUserPassword(String login, String oldPassword, String newPassword) {
        User userPassword = userRepository.getUserPassword(login);
        if (!passwordEncoder.matches(oldPassword, userPassword.getPassword())) {
            LOG.warn(StringUtils.join("Error updating password for user ", login, ". Password mismatch."));
            return SimpleResponse.fail("INVALID_PASSWORD");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        Query query = Query.query(Criteria.where("email").is(login));
        Update update = Update.update("password", encodedPassword);
        UpdateResult updatedResult = mongoTemplate.updateFirst(query, update, User.class);
        if (updatedResult.getModifiedCount() != 1) {
            LOG.error(StringUtils.join("Error updating password for user ", login, ". Update result count=", updatedResult.getModifiedCount()));
            return SimpleResponse.fail();
        }
        if (!passwordEncoder.matches(newPassword, encodedPassword)) {
            LOG.error(StringUtils.join("Error updating password for user ", login, ". After update new password is not satisfied."));
            return SimpleResponse.fail();
        }

        return SimpleResponse.success();
    }

    @Override
    public JwtUser loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.authenticateUser(username)
                .map(user -> new JwtUser(username, user.getPassword(), user.getRoles()))
                .orElse(new JwtUser());
    }

    @Override
    public User getFullUserProfile(String login) {
        return userRepository.findById(login).get();
    }

    @Override
    public Authentication getAuthentication(String login) {
        return userRepository.getAuthenticatedUser(login)
                .map(user -> new JwtUser(login, null, user.getRoles()))
                .map(jwtUser -> new UsernamePasswordAuthenticationToken(jwtUser, null, jwtUser.getAuthorities()))
                .orElseThrow(() -> new RuntimeException("UserService.getAuthentication doesn't have user retrieved"));
    }

    @Override
    public SimpleResponse includeCurrency(String login, Currency currency) {
        List<UserCurrency> currencies = userRepository.getUserCurrencies(login).getCurrencies();
        if (currencies.stream().anyMatch(userCurrency -> userCurrency.getName().equals(currency))) {
            return SimpleResponse.fail("ALREADY_EXIST");
        }
        Optional<UserCurrency> maxOrder = currencies.stream().max(Comparator.comparingInt(UserCurrency::getOrder));
        UserCurrency newCurrency = new UserCurrency(currency, false, maxOrder.map(UserCurrency::getOrder).orElse(0) + 1);

        Query query = Query.query(Criteria.where("email").is(login));
        Update update = new Update().addToSet("currencies", newCurrency);
        SimpleResponse result = updateUser(query, update);
        if (result.isSuccess() && currencies.isEmpty()) {
            markCurrencyAsDefault(login, currency);
        }

        return result;
    }

    @Override
    public SimpleResponse excludeCurrency(String login, Currency currency) {
        List<UserCurrency> currencies = userRepository.getUserCurrencies(login).getCurrencies();
        Optional<UserCurrency> currencyItem = currencies.stream().filter(userCurrency -> userCurrency.getName().equals(currency)).findFirst();

        Query query = Query.query(Criteria.where("email").is(login));
        Update update = new Update().pull("currencies", currencyItem.get()).unset("accounts.$[].subAccounts.$[].balance." + currency.name());
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        if (updateResult.getModifiedCount() != 1) {
            LOG.error("Error updating user profile - removing currency. Number of updated items " + updateResult.getModifiedCount());
            return SimpleResponse.fail();
        }

        if (currencyItem.get().isDefaultCurrency() && currencies.size() > 1) {
            Update newDefaultUpdate = new Update().set(StringUtils.join("currencies.0.defaultCurrency"), true);
            UpdateResult newDefaultUpdateResult = mongoTemplate.updateFirst(query, newDefaultUpdate, User.class);
            if (newDefaultUpdateResult.getModifiedCount() != 1) {
                LOG.error("Error updating user profile - removing currency (setting new default currency). Number of updated items " + updateResult.getModifiedCount());
                return SimpleResponse.fail();
            }
        }

        archiveHistoryItems(login, currency, true);
        return SimpleResponse.success();
    }

    @Override
    public SimpleResponse markCurrencyAsDefault(String login, Currency currency) {
        List<UserCurrency> currencies = userRepository.getUserCurrencies(login).getCurrencies();
        Optional<UserCurrency> oldDefaultCurrency = currencies.stream().filter(UserCurrency::isDefaultCurrency).findFirst();
        if (!oldDefaultCurrency.isPresent()) {
            LOG.error("There is no default currency for user " + login);
        }

        Optional<UserCurrency> defaultCurrency = currencies.stream().filter(userCurrency -> userCurrency.getName().equals(currency)).findFirst();
        if (!defaultCurrency.isPresent()) {
            LOG.error(StringUtils.join("User ", login, " doesn't have currency which is requested to be default ", currency));
            return SimpleResponse.fail();
        }
        if (defaultCurrency.equals(oldDefaultCurrency)) {
            return SimpleResponse.success();
        }

        Query query = Query.query(Criteria.where("email").is(login));
        Update update = new Update().set(StringUtils.join("currencies.", currencies.indexOf(defaultCurrency.get()), ".defaultCurrency"), true);
        oldDefaultCurrency.ifPresent(userCurrency -> update.set(StringUtils.join("currencies.", currencies.indexOf(userCurrency), ".defaultCurrency"), false));
        return updateUser(query, update);
    }

    @Override
    public SimpleResponse moveCurrency(String login, Currency currency, Direction direction) {
        List<UserCurrency> currencies = userRepository.getUserCurrencies(login).getCurrencies();
        Optional<UserCurrency> currencyItem = currencies.stream().filter(userCurrency -> userCurrency.getName().equals(currency)).findFirst();
        Optional<UserCurrency> secondCurrency = getSecondItem(currencies, direction, currencyItem.get().getOrder());;
        if (!secondCurrency.isPresent()) {
            return SimpleResponse.success();
        }

        Query query = Query.query(Criteria.where("email").is(login));
        Update update = new Update()
                .set(StringUtils.join("currencies.", currencies.indexOf(secondCurrency.get()), ".order"), currencyItem.get().getOrder())
                .set(StringUtils.join("currencies.", currencies.indexOf(currencyItem.get()), ".order"), secondCurrency.get().getOrder());
        return updateUser(query, update);
    }

    @Override
    public SimpleResponse addAccount(String login, String title) {
        List<Account> accounts = userRepository.getUserAccounts(login).getAccounts();
        if (accounts.stream().anyMatch(userAccount -> StringUtils.equals(userAccount.getTitle(), title))) {
            return SimpleResponse.fail("ALREADY_EXIST");
        }

        int order = 1 + accounts.stream()
                .max(Comparator.comparingInt(Account::getOrder))
                .map(Account::getOrder)
                .orElse(0);
        Query query = Query.query(Criteria.where("email").is(login));
        Update update = new Update().addToSet("accounts", new Account(title, order));
        return updateUser(query, update);
    }

    @Override
    public SimpleResponse editAccount(String login, String newTitle, String oldTitle) {
        List<Account> accounts = userRepository.getUserAccounts(login).getAccounts();
        Account account = chooseItem(accounts, oldTitle, getAccountError(login, oldTitle));
        if (accounts.stream().anyMatch(userAccount -> StringUtils.equals(userAccount.getTitle(), newTitle))) {
            return SimpleResponse.fail("ALREADY_EXIST");
        }

        Query query = Query.query(Criteria.where("email").is(login));
        Update update = new Update().set(StringUtils.join("accounts.", accounts.indexOf(account), ".title"), newTitle);
        SimpleResponse response = updateUser(query, update);
        if (response.isSuccess()) {
            Query historyQuery = Query.query(Criteria.where("user").is(login).and("balance.account").is(oldTitle));
            Update historyUpdate = Update.update("balance.account", newTitle);
            mongoTemplate.updateMulti(historyQuery, historyUpdate, HistoryItem.class);

            historyQuery = Query.query(Criteria.where("user").is(login).and("balance.accountTo").is(oldTitle));
            historyUpdate = Update.update("balance.accountTo", newTitle);
            mongoTemplate.updateMulti(historyQuery, historyUpdate, HistoryItem.class);
        }

        return response;
    }

    @Override
    public SimpleResponse deleteAccount(String login, String title) {
        List<Account> accounts = userRepository.getUserAccounts(login).getAccounts();
        Account account = chooseItem(accounts, title, getAccountError(login, title));

        Query query = Query.query(Criteria.where("email").is(login));
        Update update = new Update().pull("accounts", account);
        SimpleResponse response = updateUser(query, update);
        if (response.isSuccess()) {
            Query historyQuery = Query.query(Criteria.where("user").is(login)
                    .orOperator(Criteria.where("balance.account").is(title), Criteria.where("balance.accountTo").is(title)));
            archiveHistoryItems(historyQuery);

            account.getSubAccounts().forEach(subAccount -> subAccount.getBalance().forEach((currency, value) -> {
                historyService.addBalanceHistoryItem(login, currency, title, subAccount.getTitle(), () -> value * -1);
            }));
        }

        return response;
    }

    @Override
    public SimpleResponse moveAccount(String login, String title, Direction direction) {
        List<Account> accounts = userRepository.getUserAccounts(login).getAccounts();
        Account account = chooseItem(accounts, title, getAccountError(login, title));

        Optional<Account> secondAccount = getSecondItem(accounts, direction, account.getOrder());
        if (!secondAccount.isPresent()) {
            return SimpleResponse.success();
        }

        Query query = Query.query(Criteria.where("email").is(login));
        Update update = new Update()
                .set(StringUtils.join("accounts.", accounts.indexOf(secondAccount.get()), ".order"), account.getOrder())
                .set(StringUtils.join("accounts.", accounts.indexOf(account), ".order"), secondAccount.get().getOrder());
        return updateUser(query, update);
    }

    @Override
    public SimpleResponse addSubAccount(String login, String subAccountTitle, String accountTitle, String icon, Map<Currency, Double> balance) {
        List<Account> accounts = userRepository.getUserAccounts(login).getAccounts();
        Account account = chooseItem(accounts, accountTitle, getAccountError(login, accountTitle));

        List<SubAccount> subAccounts = account.getSubAccounts();
        if (subAccounts.stream().anyMatch(subAccount -> StringUtils.equals(subAccount.getTitle(), subAccountTitle))) {
            return SimpleResponse.fail("ALREADY_EXIST");
        }

        balance.entrySet().removeIf(entry -> entry.getValue() == 0);
        int order = 1 + subAccounts.stream()
                .max(Comparator.comparingInt(SubAccount::getOrder))
                .map(SubAccount::getOrder)
                .orElse(0);
        Query query = Query.query(Criteria.where("email").is(login));
        Update update = new Update().addToSet(StringUtils.join("accounts.", accounts.indexOf(account), ".subAccounts"), new SubAccount(subAccountTitle, order, icon, balance));
        SimpleResponse response = updateUser(query, update);

        if (response.isSuccess()) {
            balance.forEach((currency, value) -> {
                historyService.addBalanceHistoryItem(login, currency, accountTitle, subAccountTitle, () -> value);
            });
        }

        return response;
    }

    @Override
    public SimpleResponse changeSubAccountBalance(String login, String subAccountTitle, String accountTitle, Map<Currency, Double> balance) {
        List<Account> accounts = userRepository.getUserAccounts(login).getAccounts();
        Account account = chooseItem(accounts, accountTitle, getAccountError(login, accountTitle));

        List<SubAccount> subAccounts = account.getSubAccounts();
        SubAccount subAccount = chooseItem(subAccounts, subAccountTitle, getSubAccountError(login, accountTitle, subAccountTitle));

        balance.entrySet().removeIf(entry -> entry.getValue() == 0);
        Query query = Query.query(Criteria.where("email").is(login));
        Update update = Update.update(StringUtils.join("accounts.", accounts.indexOf(account), ".subAccounts.", subAccounts.indexOf(subAccount), ".balance"), balance);

        SimpleResponse response = updateUser(query, update);
        if (response.isSuccess()) {
            addBalanceHistoryItemOnBalanceEdit(login, accountTitle, subAccountTitle, subAccount, balance);
        }

        return response;
    }

    @Override
    public SimpleResponse editSubAccount(String login, String accountTitle, String oldSubAccountTitle, String newSubAccountTitle, String icon, Map<Currency, Double> balance) {
        List<Account> accounts = userRepository.getUserAccounts(login).getAccounts();
        Account account = chooseItem(accounts, accountTitle, getAccountError(login, accountTitle));

        List<SubAccount> subAccounts = account.getSubAccounts();
        if (!StringUtils.equals(oldSubAccountTitle, newSubAccountTitle) && subAccounts.stream().anyMatch(subAccount -> StringUtils.equals(subAccount.getTitle(), newSubAccountTitle))) {
            return SimpleResponse.fail("ALREADY_EXIST");
        }
        SubAccount subAccount = chooseItem(subAccounts, oldSubAccountTitle, getSubAccountError(login, accountTitle, oldSubAccountTitle));

        balance.entrySet().removeIf(entry -> entry.getValue() == 0);
        SubAccount newSubAccount = new SubAccount(newSubAccountTitle, subAccount.getOrder(), icon, balance);
        Query query = Query.query(Criteria.where("email").is(login));
        Update update = Update.update(StringUtils.join("accounts.", accounts.indexOf(account), ".subAccounts.", subAccounts.indexOf(subAccount)), newSubAccount);
        SimpleResponse response = updateUser(query, update);
        if (response.isSuccess()) {
            addBalanceHistoryItemOnBalanceEdit(login, accountTitle, newSubAccountTitle, subAccount, balance);

            Query historyQuery = Query.query(Criteria.where("user").is(login).and("balance.account").is(accountTitle).and("balance.subAccount").is(oldSubAccountTitle));
            Update historyUpdate = Update.update("balance.subAccount", newSubAccountTitle);
            mongoTemplate.updateMulti(historyQuery, historyUpdate, HistoryItem.class);

            historyQuery = Query.query(Criteria.where("user").is(login).and("balance.accountTo").is(accountTitle).and("balance.subAccountTo").is(oldSubAccountTitle));;
            historyUpdate = Update.update("balance.subAccountTo", newSubAccountTitle);
            mongoTemplate.updateMulti(historyQuery, historyUpdate, HistoryItem.class);
        }

        return response;
    }

    @Override
    public SimpleResponse moveSubAccount(String login, String accountTitle, String subAccountTitle, Direction direction) {
        List<Account> accounts = userRepository.getUserAccounts(login).getAccounts();
        Account account = chooseItem(accounts, accountTitle, getAccountError(login, accountTitle));

        List<SubAccount> subAccounts = account.getSubAccounts();
        SubAccount subAccount = chooseItem(subAccounts, subAccountTitle, getSubAccountError(login, accountTitle, subAccountTitle));

        Optional<SubAccount> secondSubAccount = getSecondItem(subAccounts, direction, subAccount.getOrder());
        if (!secondSubAccount.isPresent()) {
            return SimpleResponse.success();
        }

        Query query = Query.query(Criteria.where("email").is(login));
        String queryPrefix = StringUtils.join("accounts.", accounts.indexOf(account), ".subAccounts.");
        Update update = new Update()
                .set(StringUtils.join(queryPrefix, subAccounts.indexOf(secondSubAccount.get()), ".order"), subAccount.getOrder())
                .set(StringUtils.join(queryPrefix, subAccounts.indexOf(subAccount), ".order"), secondSubAccount.get().getOrder());
        return updateUser(query, update);
    }

    @Override
    public SimpleResponse toggleAccount(String login, String accountTitle, boolean toggleState) {
        List<Account> accounts = userRepository.getUserAccounts(login).getAccounts();
        Account account = chooseItem(accounts, accountTitle, getAccountError(login, accountTitle));

        Query query = Query.query(Criteria.where("email").is(login));
        Update update = Update.update(StringUtils.join("accounts.", accounts.indexOf(account), ".opened"), toggleState);
        return updateUser(query, update);
    }

    @Override
    public SimpleResponse deleteSubAccount(String login, String accountTitle, String subAccountTitle) {
        List<Account> accounts = userRepository.getUserAccounts(login).getAccounts();
        Account account = chooseItem(accounts, accountTitle, getAccountError(login, accountTitle));

        List<SubAccount> subAccounts = account.getSubAccounts();
        SubAccount subAccount = chooseItem(subAccounts, subAccountTitle, getSubAccountError(login, accountTitle, subAccountTitle));

        Query query = Query.query(Criteria.where("email").is(login));
        Update update = new Update().pull(StringUtils.join("accounts.", accounts.indexOf(account), ".subAccounts"), subAccount);
        SimpleResponse response = updateUser(query, update);
        if (response.isSuccess()) {
            Query historyQuery = Query.query(Criteria.where("user").is(login).orOperator(
                    Criteria.where("balance.account").is(accountTitle).and("balance.subAccount").is(subAccountTitle),
                    Criteria.where("balance.accountTo").is(accountTitle).and("balance.subAccountTo").is(subAccountTitle)));
            archiveHistoryItems(historyQuery);
            subAccount.getBalance().forEach((currency, value) -> historyService.addBalanceHistoryItem(login, currency, accountTitle, subAccountTitle, () -> value * -1));
        }

        return response;
    }

    @Override
    public SimpleResponse addCategory(String login, String categoryTitle, String icon) {
        List<Category> categories = userRepository.getUserCategories(login).getCategories();
        if (categories.stream().anyMatch(userCategory -> StringUtils.equals(userCategory.getTitle(), categoryTitle))) {
            return SimpleResponse.fail("ALREADY_EXIST");
        }

        int order = 1 + categories.stream()
                .max(Comparator.comparingInt(Category::getOrder))
                .map(Category::getOrder)
                .orElse(0);
        Query query = Query.query(Criteria.where("email").is(login));
        Update update = new Update().addToSet("categories", new Category(categoryTitle, icon, order));
        return updateUser(query, update);
    }

    @Override
    public SimpleResponse editCategory(String login, String oldCategoryTitle, String newCategoryTitle, String icon) {
        List<Category> categories = userRepository.getUserCategories(login).getCategories();
        Category category = chooseItem(categories, oldCategoryTitle, getAccountError(login, oldCategoryTitle));
        if (!StringUtils.equals(oldCategoryTitle, newCategoryTitle) && categories.stream().anyMatch(userCategory -> StringUtils.equals(userCategory.getTitle(), newCategoryTitle))) {
            return SimpleResponse.fail("ALREADY_EXIST");
        }
        if (StringUtils.equals(oldCategoryTitle, newCategoryTitle) && StringUtils.equals(category.getIcon(), icon)) {
            return SimpleResponse.success();
        }

        Query query = Query.query(Criteria.where("email").is(login));
        Update update = new Update()
                .set(StringUtils.join("categories.", categories.indexOf(category), ".title"), newCategoryTitle)
                .set(StringUtils.join("categories.", categories.indexOf(category), ".icon"), icon);
        SimpleResponse response = updateUser(query, update);
        if (response.isSuccess()) {
            Query historyQuery = Query.query(Criteria.where("user").is(login).and("category").is(oldCategoryTitle));
            Update historyUpdate = Update.update("category", newCategoryTitle);
            mongoTemplate.updateMulti(historyQuery, historyUpdate, HistoryItem.class);
        }

        return response;
    }

    @Override
    public SimpleResponse deleteCategory(String login, String categoryTitle) {
        List<Category> categories = userRepository.getUserCategories(login).getCategories();
        Category category = chooseItem(categories, categoryTitle, getAccountError(login, categoryTitle));

        Query query = Query.query(Criteria.where("email").is(login));
        Update update = new Update().pull("categories", category);
        SimpleResponse response = updateUser(query, update);
        if (response.isSuccess()) {
            Query historyQuery = Query.query(Criteria.where("user").is(login).and("category").is(categoryTitle));
            archiveHistoryItems(historyQuery);
        }

        return response;
    }

    @Override
    public SimpleResponse moveCategory(String login, String categoryTitle, Direction direction) {
        List<Category> categories = userRepository.getUserCategories(login).getCategories();
        Category category = chooseItem(categories, categoryTitle, getAccountError(login, categoryTitle));

        Optional<Category> secondCategory = getSecondItem(categories, direction, category.getOrder());
        if (!secondCategory.isPresent()) {
            return SimpleResponse.success();
        }

        Query query = Query.query(Criteria.where("email").is(login));
        Update update = new Update()
                .set(StringUtils.join("categories.", categories.indexOf(secondCategory.get()), ".order"), category.getOrder())
                .set(StringUtils.join("categories.", categories.indexOf(category), ".order"), secondCategory.get().getOrder());
        return updateUser(query, update);
    }

    @Override
    public SimpleResponse addSubCategory(String login, String categoryTitle, String subCategoryTitle, SubCategoryType subCategoryType) {
        List<Category> categories = userRepository.getUserCategories(login).getCategories();
        Category category = chooseItem(categories, categoryTitle, getAccountError(login, categoryTitle));

        List<SubCategory> subCategories = category.getSubCategories();
        if (subCategories.stream().anyMatch(subCategory -> subCategoryType.equals(subCategory.getType()) && StringUtils.equals(subCategory.getTitle(), subCategoryTitle))) {
            return SimpleResponse.fail("ALREADY_EXIST");
        }

        int order = 1 + subCategories.stream()
                .max(Comparator.comparingInt(SubCategory::getOrder))
                .map(SubCategory::getOrder)
                .orElse(0);
        Query query = Query.query(Criteria.where("email").is(login));
        Update update = new Update().addToSet(StringUtils.join("categories.", categories.indexOf(category), ".subCategories"), new SubCategory(subCategoryTitle, order, subCategoryType));
        return updateUser(query, update);
    }

    @Override
    public SimpleResponse editSubCategory(String login, String categoryTitle, String oldSubCategoryTitle, String newSubCategoryTitle, SubCategoryType subCategoryType) {
        List<Category> categories = userRepository.getUserCategories(login).getCategories();
        Category category = chooseItem(categories, categoryTitle, getAccountError(login, categoryTitle));

        List<SubCategory> subCategories = category.getSubCategories();
        if (!StringUtils.equals(oldSubCategoryTitle, newSubCategoryTitle) && subCategories.stream().anyMatch(subCategory -> subCategoryType.equals(subCategory.getType()) && StringUtils.equals(subCategory.getTitle(), newSubCategoryTitle))) {
            return SimpleResponse.fail("ALREADY_EXIST");
        }

        SubCategory subCategory = chooseSubCategory(subCategories, oldSubCategoryTitle, subCategoryType, getSubAccountError(login, categoryTitle, oldSubCategoryTitle));
        subCategory.setTitle(newSubCategoryTitle);

        Query query = Query.query(Criteria.where("email").is(login));
        Update update = Update.update(StringUtils.join("categories.", categories.indexOf(category), ".subCategories.", subCategories.indexOf(subCategory)), subCategory);
        SimpleResponse response = updateUser(query, update);
        if (response.isSuccess()) {
            Query historyQuery = Query.query(Criteria.where("user").is(login).and("category").is(categoryTitle).and("subCategory").is(oldSubCategoryTitle).and("type").is(subCategoryType.name()));
            Update historyUpdate = Update.update("subCategory", newSubCategoryTitle);
            mongoTemplate.updateMulti(historyQuery, historyUpdate, HistoryItem.class);
        }

        return response;
    }

    @Override
    public SimpleResponse deleteSubCategory(String login, String categoryTitle, String subCategoryTitle, SubCategoryType subCategoryType) {
        List<Category> categories = userRepository.getUserCategories(login).getCategories();
        Category category = chooseItem(categories, categoryTitle, getAccountError(login, categoryTitle));

        List<SubCategory> subCategories = category.getSubCategories();
        SubCategory subCategory = chooseSubCategory(subCategories, subCategoryTitle, subCategoryType, getSubAccountError(login, categoryTitle, subCategoryTitle));

        Query query = Query.query(Criteria.where("email").is(login));
        Update update = new Update().pull(StringUtils.join("categories.", categories.indexOf(category), ".subCategories"), subCategory);
        SimpleResponse response = updateUser(query, update);
        if (response.isSuccess()) {
            Query historyQuery = Query.query(Criteria.where("user").is(login)
                    .and("category").is(categoryTitle)
                    .and("subCategory").is(subCategoryTitle)
                    .and("type").is(subCategoryType.name()));
            archiveHistoryItems(historyQuery);
        }

        return response;
    }

    @Override
    public SimpleResponse moveSubCategory(String login, String categoryTitle, String subCategoryTitle, SubCategoryType subCategoryType, Direction direction) {
        List<Category> categories = userRepository.getUserCategories(login).getCategories();
        Category category = chooseItem(categories, categoryTitle, getAccountError(login, categoryTitle));

        List<SubCategory> subCategories = category.getSubCategories();
        SubCategory subCategory = chooseSubCategory(subCategories, subCategoryTitle, subCategoryType, getSubAccountError(login, categoryTitle, subCategoryTitle));

        Optional<SubCategory> secondSubCategory = getSecondItem(subCategories, direction, subCategory.getOrder());
        if (!secondSubCategory.isPresent()) {
            return SimpleResponse.success();
        }

        Query query = Query.query(Criteria.where("email").is(login));
        String queryPrefix = StringUtils.join("categories.", categories.indexOf(category), ".subCategories.");
        Update update = new Update()
                .set(StringUtils.join(queryPrefix, subCategories.indexOf(secondSubCategory.get()), ".order"), subCategory.getOrder())
                .set(StringUtils.join(queryPrefix, subCategories.indexOf(subCategory), ".order"), secondSubCategory.get().getOrder());
        return updateUser(query, update);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    @Override
    public SimpleResponse addUser(String email, String password, List<UserPermission> roles) {
        if (userRepository.existsById(email)) {
            return SimpleResponse.fail("ALREADY_EXIST");
        }

        userRepository.save(new User(email, passwordEncoder.encode(password), roles));
        return SimpleResponse.success();
    }

    @Override
    public SimpleResponse editUser(String email, String password, List<UserPermission> roles) {
        Query query = Query.query(Criteria.where("email").is(email));
        Update update = new Update().set("roles", roles);
        if (StringUtils.isNotBlank(password)) {
            update.set("password", passwordEncoder.encode(password));
        }
        return updateUser(query, update);
    }

    @Override
    public SimpleResponse deleteUser(String email) {
        userRepository.deleteById(email);
        mongoTemplate.remove(Query.query(Criteria.where("user").is(email)), HistoryItem.class);
        return SimpleResponse.success();
    }

    @Override
    public SimpleResponse updateUserBalance(String login, HistoryType type, Balance historyBalance) {
//        db.users.updateOne({"_id" : "skoval@gmail.com"},
//            {$inc: {"accounts.$[par].subAccounts.$[sub].balance.BYN": NumberInt(10)}},
//            {arrayFilters: [{"par.title":"Новый2"}, {"sub.title": "не пустой1"}]})

        String accountTitle = historyBalance.getAccount();
        List<Account> accounts = userRepository.getUserAccounts(login).getAccounts();
        Account account = chooseItem(accounts, accountTitle, getAccountError(login, accountTitle));

        Currency currency = historyBalance.getCurrency();
        String subAccountTitle = historyBalance.getSubAccount();
        List<SubAccount> subAccounts = account.getSubAccounts();
        SubAccount subAccount = chooseItem(subAccounts, subAccountTitle, getSubAccountError(login, accountTitle, subAccountTitle));

        Update update = null;
        Query query = Query.query(Criteria.where("email").is(login));
        switch (type) {
            case expense:
                historyBalance.setValue(historyBalance.getValue() * -1);
            case income:
                String updateQuery = StringUtils.join("accounts.", accounts.indexOf(account), ".subAccounts.", subAccounts.indexOf(subAccount), ".balance.", currency);
                update = new Update().inc(updateQuery, historyBalance.getValue());
                break;
            case transfer:
                Account accountTo = chooseItem(accounts, historyBalance.getAccountTo(), getAccountError(login, historyBalance.getAccountTo()));
                List<SubAccount> subAccountsTo = accountTo.getSubAccounts();
                SubAccount subAccountTo = chooseItem(subAccountsTo, historyBalance.getSubAccountTo(), getSubAccountError(login, historyBalance.getAccountTo(), historyBalance.getSubAccountTo()));

                String updateQueryFrom = StringUtils.join("accounts.", accounts.indexOf(account), ".subAccounts.", subAccounts.indexOf(subAccount), ".balance.", currency);
                String updateQueryTo = StringUtils.join("accounts.", accounts.indexOf(accountTo), ".subAccounts.", subAccountsTo.indexOf(subAccountTo), ".balance.", currency);
                update = new Update().inc(updateQueryFrom, historyBalance.getValue() * -1).inc(updateQueryTo, historyBalance.getValue());
                break;
            case exchange:
                String updateQueryOld = StringUtils.join("accounts.", accounts.indexOf(account), ".subAccounts.", subAccounts.indexOf(subAccount), ".balance.", currency);
                String updateQueryNew = StringUtils.join("accounts.", accounts.indexOf(account), ".subAccounts.", subAccounts.indexOf(subAccount), ".balance.", historyBalance.getNewCurrency());
                update = new Update().inc(updateQueryOld, historyBalance.getValue() * -1).inc(updateQueryNew, historyBalance.getNewValue());
                break;
        }

        return updateUser(query, update);
    }

    private <T extends Orderable> Optional<T> getSecondItem(List<T> items, Direction direction, int itemOrder) {
        Optional<T> secondItem;
        switch (direction) {
            case DOWN:
                secondItem = items.stream()
                        .filter(item -> item.getOrder() > itemOrder)
                        .min(Comparator.comparingInt(Orderable::getOrder));
                break;
            case UP:
                secondItem = items.stream()
                        .filter(item -> item.getOrder() < itemOrder)
                        .max(Comparator.comparingInt(Orderable::getOrder));
                break;
            default:
                secondItem = Optional.empty();
        }

        return secondItem;
    }

    private SubCategory chooseSubCategory(List<SubCategory> items, String title, SubCategoryType subCategoryType, Supplier<String> errorMessage) {
        return items.stream()
                .filter(checkItem -> subCategoryType.equals(checkItem.getType()) && StringUtils.equals(checkItem.getTitle(), title))
                .findFirst()
                .orElseThrow(() -> new SelectableItemMissedSettingUpdateException(errorMessage.get()));
    }

    private <T extends Selectable> T chooseItem(List<T> items, String title, Supplier<String> errorMessage) {
        return items.stream()
                .filter(checkItem -> StringUtils.equals(checkItem.getTitle(), title))
                .findFirst()
                .orElseThrow(() -> new SelectableItemMissedSettingUpdateException(errorMessage.get()));
    }

    private SimpleResponse updateUser(Query query, Update update) {
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        if (updateResult.getModifiedCount() != 1) {
            LOG.error("Error updating user profile. Number of updated items " + updateResult.getModifiedCount());
            return SimpleResponse.fail();
        }

        return SimpleResponse.success();
    }

    private Supplier<String> getAccountError(String login, String accountTitle) {
        return () -> StringUtils.join("Account ", accountTitle, " is missed for user ", login);
    }

    private Supplier<String> getSubAccountError(String login, String accountTitle, String subAccountTitle) {
        return () -> StringUtils.join("Sub account ", subAccountTitle, " for account ", accountTitle, " is missed for user ", login);
    }

    private void archiveHistoryItems(String login, Currency currency, boolean archive) {
        Query historyQuery = Query.query(Criteria.where("user").is(login)
                .orOperator(Criteria.where("balance.currency").is(currency), Criteria.where("balance.newCurrency").is(currency)));
        Update historyUpdate = new Update().set("archived", archive);
        mongoTemplate.updateMulti(historyQuery, historyUpdate, HistoryItem.class);
    }

    private void archiveHistoryItems(Query query) {
        Update historyUpdate = new Update().set("archived", true);
        mongoTemplate.updateMulti(query, historyUpdate, HistoryItem.class);
    }

    private void addBalanceHistoryItemOnBalanceEdit(String login, String accountTitle, String subAccountTitle, SubAccount subAccount, Map<Currency, Double> balance) {
        subAccount.getBalance().forEach((currency, value) -> {
            Double currencyValue = !balance.containsKey(currency) ? value * -1 : Double.parseDouble(CURRENCY_FORMAT.format(balance.get(currency) - value));
            if (currencyValue != 0) {
                historyService.addBalanceHistoryItem(login, currency, accountTitle, subAccountTitle, () -> currencyValue);
            }
        });

        CollectionUtils.disjunction(subAccount.getBalance().keySet(), balance.keySet()).forEach(currency -> {
            if (balance.containsKey(currency)) {
                historyService.addBalanceHistoryItem(login, currency, accountTitle, subAccountTitle, () -> balance.get(currency));
            }
        });
    }
}