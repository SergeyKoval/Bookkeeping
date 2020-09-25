package by.bk.entity.user;

import by.bk.controller.model.request.ChangeDeviceNameRequest;
import by.bk.controller.model.request.Direction;
import by.bk.controller.model.request.SubAccountAssignmentRequest;
import by.bk.controller.model.response.SimpleResponse;
import by.bk.entity.budget.BudgetAPI;
import by.bk.entity.budget.model.Budget;
import by.bk.entity.currency.Currency;
import by.bk.entity.history.*;
import by.bk.entity.history.Balance;
import by.bk.entity.user.exception.SelectableItemMissedSettingUpdateException;
import by.bk.entity.user.model.*;
import by.bk.mail.EmailPreparator;
import by.bk.security.MissedUserException;
import by.bk.security.model.JwtToken;
import by.bk.security.model.JwtUser;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.function.Supplier;

/**
 * @author Sergey Koval
 */
@Service
public class UserService implements UserAPI, UserDetailsService {
    private static final Log LOG = LogFactory.getLog(UserService.class);
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("##0.00", DecimalFormatSymbols.getInstance(Locale.US));

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HistoryAPI historyApi;
    @Autowired
    private BudgetAPI budgetAPI;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private EmailPreparator registrationCodeEmailPreparator;

    @Override
    public SimpleResponse sendRegistrationCode(String email, String password, boolean restorePassword) {
        Optional<User> optionalUser = userRepository.findById(email);
        if (restorePassword && !optionalUser.isPresent()) {
            throw new MissedUserException(email);
        }

        User user = optionalUser.orElseGet(() -> initUserFromTemplate(email, password, Collections.singletonList(UserPermission.USER)));
        if (!restorePassword && user.isEnabled()) {
            return SimpleResponse.alreadyExistsFail();
        }

        if (restorePassword) {
            user.setEnabled(false);
        }
        user.setCode(Long.toString(RandomUtils.nextLong(1000, 10000)));
        userRepository.save(user);
        return registrationCodeEmailPreparator.prepareAndSend(email, user.getCode()) ? SimpleResponse.success() : SimpleResponse.fail();
    }

    @Override
    public SimpleResponse reviewRegistrationCode(String email, String password, String code, Supplier<String> tokenSupplier) {
        User user = userRepository.findById(email).orElseThrow(() -> new MissedUserException(email));
        if (!StringUtils.equals(code, user.getCode())) {
            return SimpleResponse.fail("INVALID_CODE");
        }

        user.setEnabled(true);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        return SimpleResponse.success(tokenSupplier.get());
    }

    @Override
    public void registerDevice(String email, String deviceId, String token) {
        Query query = Query.query(Criteria.where("email").is(email));
        Update update = Update.update(StringUtils.join("devices.", deviceId, ".token"), token);
        if (mongoTemplate.updateFirst(query, update, User.class).getModifiedCount() != 1) {
            throw new RuntimeException("Fail to store device authentication for the user " + email);
        }
    }

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
                .map(user -> new JwtUser(username, user.getPassword(), user.isEnabled(), user.getRoles(), null))
                .orElseThrow(() -> new MissedUserException(username));
    }

    @Override
    public User getFullUserProfile(String login) {
        return userRepository.findById(login).get();
    }

    @Override
    public List<Account> getAccountsSummary(String login, String deviceId) {
        List<Account> accounts = userRepository.getUserAccounts(login).getAccounts();
        accounts.stream()
                .flatMap(account -> account.getSubAccounts().stream())
                .forEach(subAccount -> {
                    subAccount.setBalance(null);
                    List<DeviceAssociation> deviceAssociations = subAccount.getDevice().get(deviceId);
                    subAccount.setDevice(deviceAssociations != null && !deviceAssociations.isEmpty() ? Collections.singletonMap(deviceId, deviceAssociations) : null);
                });
        return accounts;
    }

    @Override
    public Authentication getAuthentication(JwtToken token) {
        User authenticatedUser = userRepository.getAuthenticatedUser(token.getUsername());
        if (authenticatedUser == null) {
            LOG.warn("UserService.getAuthentication doesn't have user retrieved or user doesn't suit");
            return new UsernamePasswordAuthenticationToken(null, null);
        }

        if (StringUtils.isNotBlank(token.getDeviceId())) {
            Device device = authenticatedUser.getDevices().get(token.getDeviceId());
            if (device == null || !StringUtils.equals(device.getToken(), token.getToken())) {
                LOG.error(StringUtils.join("User ", token.getUsername(), " with deviceId ", token.getDeviceId(), " is trying to call API. Device authenticated = ", device != null));
                return new UsernamePasswordAuthenticationToken(null, null);
            }
        }

        List<UserPermission> permissions = StringUtils.isBlank(token.getDeviceId()) ? authenticatedUser.getRoles() : List.of(UserPermission.MOBILE);
        JwtUser jwtUser = new JwtUser(token.getUsername(), null, authenticatedUser.isEnabled(), permissions, token.getDeviceId());
        return new UsernamePasswordAuthenticationToken(jwtUser, null, jwtUser.getAuthorities());
    }

    @Override
    public SimpleResponse includeCurrency(String login, Currency currency) {
        List<UserCurrency> currencies = userRepository.getUserCurrencies(login).getCurrencies();
        if (currencies.stream().anyMatch(userCurrency -> userCurrency.getName().equals(currency))) {
            return SimpleResponse.alreadyExistsFail();
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
            return SimpleResponse.alreadyExistsFail();
        }

        // TODO: 23-Nov-18 mapToInt.max
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
            return SimpleResponse.alreadyExistsFail();
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
        Update update = new Update().pull("accounts", Collections.singletonMap("title", account.getTitle()));
        SimpleResponse response = updateUser(query, update);
        if (response.isSuccess()) {
            Query historyQuery = Query.query(Criteria.where("user").is(login)
                    .orOperator(Criteria.where("balance.account").is(title), Criteria.where("balance.accountTo").is(title)));
            archiveHistoryItems(historyQuery);

            account.getSubAccounts().forEach(subAccount -> subAccount.getBalance().forEach((currency, value) -> {
                historyApi.addBalanceHistoryItem(login, currency, title, subAccount.getTitle(), () -> value * -1);
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
            return SimpleResponse.alreadyExistsFail();
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
                historyApi.addBalanceHistoryItem(login, currency, accountTitle, subAccountTitle, () -> value);
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

        balance.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue() == 0);
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
            return SimpleResponse.alreadyExistsFail();
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
        Update update = new Update().pull(StringUtils.join("accounts.", accounts.indexOf(account), ".subAccounts"), Collections.singletonMap("title", subAccount.getTitle()));
        SimpleResponse response = updateUser(query, update);
        if (response.isSuccess()) {
            Query historyQuery = Query.query(Criteria.where("user").is(login).orOperator(
                    Criteria.where("balance.account").is(accountTitle).and("balance.subAccount").is(subAccountTitle),
                    Criteria.where("balance.accountTo").is(accountTitle).and("balance.subAccountTo").is(subAccountTitle)));
            archiveHistoryItems(historyQuery);
            subAccount.getBalance().forEach((currency, value) -> historyApi.addBalanceHistoryItem(login, currency, accountTitle, subAccountTitle, () -> value * -1));
        }

        return response;
    }

    @Override
    public SimpleResponse addCategory(String login, String categoryTitle, String icon) {
        List<Category> categories = userRepository.getUserCategories(login).getCategories();
        if (categories.stream().anyMatch(userCategory -> StringUtils.equals(userCategory.getTitle(), categoryTitle))) {
            return SimpleResponse.alreadyExistsFail();
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
            return SimpleResponse.alreadyExistsFail();
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

            response = budgetAPI.renameCategory(login, oldCategoryTitle, newCategoryTitle);
        }

        return response;
    }

    @Override
    public SimpleResponse deleteCategory(String login, String categoryTitle) {
        List<Category> categories = userRepository.getUserCategories(login).getCategories();
        Category category = chooseItem(categories, categoryTitle, getAccountError(login, categoryTitle));

        Query query = Query.query(Criteria.where("email").is(login));
        Update update = new Update().pull("categories", Collections.singletonMap("title", category.getTitle()));
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
            return SimpleResponse.alreadyExistsFail();
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
            return SimpleResponse.alreadyExistsFail();
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
    public SimpleResponse moveSubCategoryToAnotherCategory(String login, String oldCategoryTitle, String newCategoryTitle, String subCategoryTitle, SubCategoryType subCategoryType) {
        List<Category> categories = userRepository.getUserCategories(login).getCategories();
        Category oldCategory = chooseItem(categories, oldCategoryTitle, getAccountError(login, oldCategoryTitle));
        Category newCategory = chooseItem(categories, newCategoryTitle, getAccountError(login, newCategoryTitle));
        int newSubCategoryOrder = newCategory.getSubCategories().stream().mapToInt(SubCategory::getOrder).max().orElse(0) + 1;
        SubCategory subCategory = chooseSubCategory(oldCategory.getSubCategories(), subCategoryTitle, subCategoryType, getSubAccountError(login, oldCategoryTitle, subCategoryTitle));

        Query query = Query.query(Criteria.where("email").is(login));
        Update update = new Update()
                .pull("categories." + categories.indexOf(oldCategory) + ".subCategories", subCategory)
                .push("categories." + categories.indexOf(newCategory) + ".subCategories", new SubCategory(subCategory.getTitle(), newSubCategoryOrder, subCategory.getType()));
        SimpleResponse response = updateUser(query, update);

        if (response.isSuccess()) {
            List<HistoryItem> historyItems = historyApi.getSuitable(login, oldCategoryTitle, subCategoryTitle, subCategoryType);
            response = budgetAPI.moveCategory(login, oldCategoryTitle, newCategoryTitle, subCategoryTitle, subCategoryType, historyItems);
        }

        return response;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    @Override
    public SimpleResponse addUser(String email, String password, List<UserPermission> roles) {
        if (userRepository.existsById(email)) {
            return SimpleResponse.alreadyExistsFail();
        }

        userRepository.save(initUserFromTemplate(email, password, roles));
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
        mongoTemplate.remove(Query.query(Criteria.where("user").is(email)), Budget.class);
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

    @Override
    public SimpleResponse assignSubAccount(String login, String deviceId, SubAccountAssignmentRequest subAccountAssignment) {
        String accountTitle = subAccountAssignment.getAccount();
        String subAccountTitle = subAccountAssignment.getSubAccount();
        DeviceAssociation deviceAssociation = new DeviceAssociation(subAccountAssignment.getSender(), subAccountAssignment.getSubAccountIdentifier());

        List<Account> accounts = userRepository.getUserAccounts(login).getAccounts();
        Account account = chooseItem(accounts, accountTitle, getAccountError(login, accountTitle));
        List<SubAccount> subAccounts = account.getSubAccounts();
        SubAccount subAccount = chooseItem(subAccounts, subAccountTitle, getSubAccountError(login, accountTitle, subAccountTitle));

        List<DeviceAssociation> deviceAssociations = subAccount.getDevice().get(deviceId);
        if (deviceAssociations != null && deviceAssociations.contains(deviceAssociation)) {
            return SimpleResponse.success();
        }

        Query query = Query.query(Criteria.where("email").is(login));
        String setKey = StringUtils.join("accounts.", accounts.indexOf(account), ".subAccounts.", subAccounts.indexOf(subAccount), ".device.", deviceId);
        Update update = new Update().addToSet(setKey, deviceAssociation);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        if (updateResult.getModifiedCount() != 1) {
            LOG.error("Error updating user profile - assign sub account. Number of updated items " + updateResult.getModifiedCount());
            return SimpleResponse.fail();
        }

        return SimpleResponse.success();
    }

    @Override
    public SimpleResponse deassignSubAccount(String login, String deviceId, SubAccountAssignmentRequest subAccountAssignment) {
        String accountTitle = subAccountAssignment.getAccount();
        String subAccountTitle = subAccountAssignment.getSubAccount();

        List<Account> accounts = userRepository.getUserAccounts(login).getAccounts();
        Account account = chooseItem(accounts, accountTitle, getAccountError(login, accountTitle));
        List<SubAccount> subAccounts = account.getSubAccounts();
        SubAccount subAccount = chooseItem(subAccounts, subAccountTitle, getSubAccountError(login, accountTitle, subAccountTitle));

        Query query = Query.query(Criteria.where("email").is(login));
        String key = StringUtils.join("accounts.", accounts.indexOf(account), ".subAccounts.", subAccounts.indexOf(subAccount), ".device.", deviceId);
        Update update = new Update().pull(key, new DeviceAssociation(subAccountAssignment.getSender(), subAccountAssignment.getSubAccountIdentifier()));
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        if (updateResult.getModifiedCount() != 1) {
            LOG.error("Error updating user profile - assign sub account. Number of updated items " + updateResult.getModifiedCount());
            return SimpleResponse.fail();
        }

        return SimpleResponse.success();
    }

    @Override
    public SimpleResponse changeDeviceName(String login, ChangeDeviceNameRequest deviceDetails) {
        Query query = Query.query(Criteria.where("_id").is(login));
        String setKey = StringUtils.join("devices.", deviceDetails.getDeviceId(), ".name");
        Update update = new Update().set(setKey, deviceDetails.getName());
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        if (updateResult.getModifiedCount() != 1) {
            LOG.error("Error updating user profile - change device name. Number of updated items " + updateResult.getModifiedCount());
            return SimpleResponse.fail();
        }

        return SimpleResponse.success();
    }

    @Override
    public SimpleResponse logoutDevice(String login, String deviceId) {
        Query query = Query.query(Criteria.where("_id").is(login));
        Update update = new Update().unset(StringUtils.join("devices.", deviceId, ".token"));
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        if (updateResult.getModifiedCount() != 1) {
            LOG.error("Error updating user profile - logout device. Number of updated items " + updateResult.getModifiedCount());
            return SimpleResponse.fail();
        }

        return SimpleResponse.success();
    }

    @Override
    public SimpleResponse removeDevice(String login, String deviceId) {
        Query query = Query.query(Criteria.where("_id").is(login));
        Update update = new Update().unset(StringUtils.join("devices.", deviceId));
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        if (updateResult.getModifiedCount() != 1) {
            LOG.error("Error updating user profile - remove device. Number of updated items " + updateResult.getModifiedCount());
            return SimpleResponse.fail();
        }

        return SimpleResponse.success();
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
                historyApi.addBalanceHistoryItem(login, currency, accountTitle, subAccountTitle, () -> currencyValue);
            }
        });

        CollectionUtils.disjunction(subAccount.getBalance().keySet(), balance.keySet()).forEach(currency -> {
            if (balance.containsKey(currency)) {
                historyApi.addBalanceHistoryItem(login, currency, accountTitle, subAccountTitle, () -> balance.get(currency));
            }
        });
    }

    private User initUserFromTemplate(String email, String password, List<UserPermission> roles) {
        User user = new User(email, passwordEncoder.encode(password), roles);
        user.setCurrencies(Collections.singletonList(new UserCurrency(Currency.BYN, true, 1)));

        List<Account> accounts = new ArrayList<>();
        user.setAccounts(accounts);
        Account mainAccount = new Account("Деньги", 1);
        mainAccount.setOpened(true);
        mainAccount.getSubAccounts().add(new SubAccount("Наличные", 1, "Money.gif", new HashMap<>()));
        mainAccount.getSubAccounts().add(new SubAccount("Альфа банк", 2, "alfa.gif", new HashMap<>()));
        mainAccount.getSubAccounts().add(new SubAccount("Беларусьбанк", 3, "belarusbank.gif", new HashMap<>()));
        Account additionalAccount = new Account("Долги", 2);
        additionalAccount.getSubAccounts().add(new SubAccount("Дядя Вася", 1, "rabotnik.gif", new HashMap<>()));
        additionalAccount.getSubAccounts().add(new SubAccount("Вера Пупкина", 2, "collega.gif", new HashMap<>()));
        accounts.add(mainAccount);
        accounts.add(additionalAccount);

        List<Category> categories = new ArrayList<>();
        user.setCategories(categories);
        Category category = new Category("Автомобиль", "avto.gif", 1);
        category.getSubCategories().add(new SubCategory("Бензин", 1, SubCategoryType.expense));
        category.getSubCategories().add(new SubCategory("Мойка", 2, SubCategoryType.expense));
        category.getSubCategories().add(new SubCategory("Штраф", 3, SubCategoryType.expense));
        categories.add(category);
        category = new Category("Дом", "home.gif", 2);
        category.getSubCategories().add(new SubCategory("Коммуналка", 1, SubCategoryType.expense));
        category.getSubCategories().add(new SubCategory("Вода", 2, SubCategoryType.expense));
        category.getSubCategories().add(new SubCategory("Электричество", 3, SubCategoryType.expense));
        categories.add(category);
        category = new Category("Дети", "deti.gif", 3);
        category.getSubCategories().add(new SubCategory("Вещи", 1, SubCategoryType.expense));
        category.getSubCategories().add(new SubCategory("Подарки", 2, SubCategoryType.expense));
        category.getSubCategories().add(new SubCategory("Подарки", 3, SubCategoryType.income));
        category.getSubCategories().add(new SubCategory("Образование", 4, SubCategoryType.expense));
        categories.add(category);
        category = new Category("Доход", "income.gif", 4);
        category.getSubCategories().add(new SubCategory("Зарплата", 1, SubCategoryType.income));
        category.getSubCategories().add(new SubCategory("Подарки", 2, SubCategoryType.income));
        category.getSubCategories().add(new SubCategory("Проценты в банке", 3, SubCategoryType.income));
        categories.add(category);
        return user;
    }
}
