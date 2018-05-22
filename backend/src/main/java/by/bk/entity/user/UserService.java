package by.bk.entity.user;

import by.bk.controller.model.request.UpdateCurrencyRequest;
import by.bk.controller.model.response.SimpleResponse;
import by.bk.entity.currency.Currency;
import by.bk.entity.user.model.Account;
import by.bk.entity.user.model.User;
import by.bk.entity.user.model.UserCurrency;
import by.bk.security.model.JwtUser;
import com.mongodb.client.result.UpdateResult;
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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * @author Sergey Koval
 */
@Service
public class UserService implements UserAPI, UserDetailsService {
    private static final Log LOG = LogFactory.getLog(UserService.class);

    @Autowired
    private UserRepository userRepository;
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
            return SimpleResponse.fail("ERROR");
        }
        if (!passwordEncoder.matches(newPassword, encodedPassword)) {
            LOG.error(StringUtils.join("Error updating password for user ", login, ". After update new password is not satisfied."));
            return SimpleResponse.fail("ERROR");
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
        User user = userRepository.getUserCurrencies(login);
        if (user.getCurrencies().stream().anyMatch(userCurrency -> userCurrency.getName().equals(currency))) {
            return SimpleResponse.fail("ALREADY_EXIST");
        }
        Optional<UserCurrency> maxOrder = user.getCurrencies().stream().max(Comparator.comparingInt(UserCurrency::getOrder));
        UserCurrency newCurrency = new UserCurrency(currency, false, maxOrder.map(UserCurrency::getOrder).orElse(0) + 1);

        Query query = Query.query(Criteria.where("email").is(login));
        Update update = new Update().addToSet("currencies", newCurrency);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        if (updateResult.getModifiedCount() != 1) {
            LOG.error("Error updating user profile - adding currency. Number of updated items " + updateResult.getModifiedCount());
            return SimpleResponse.fail("ERROR");
        }

        return SimpleResponse.success();
    }

    @Override
    public SimpleResponse excludeCurrency(String login, Currency currency) {
        User user = userRepository.getUserCurrencies(login);
        List<UserCurrency> currencies = user.getCurrencies();
        Optional<UserCurrency> currencyItem = currencies.stream().filter(userCurrency -> userCurrency.getName().equals(currency)).findFirst();

        Query query = Query.query(Criteria.where("email").is(login));
        Update update = new Update().pull("currencies", Collections.singletonMap("name", currency));
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        if (updateResult.getModifiedCount() != 1) {
            LOG.error("Error updating user profile - removing currency. Number of updated items " + updateResult.getModifiedCount());
            return SimpleResponse.fail("ERROR");
        }

        if (currencyItem.get().isDefaultCurrency()) {
            Optional<UserCurrency> newDefaultCurrency = currencies.stream().filter(userCurrency -> !userCurrency.getName().equals(currency)).findFirst();
            Update newDefaultUpdate = new Update().set(StringUtils.join("currencies.", currencies.indexOf(newDefaultCurrency.get()), ".defaultCurrency"), true);
            UpdateResult newDefaultUpdateResult = mongoTemplate.updateFirst(query, newDefaultUpdate, User.class);
            if (newDefaultUpdateResult.getModifiedCount() != 1) {
                LOG.error("Error updating user profile - removing currency (setting new default currency). Number of updated items " + updateResult.getModifiedCount());
                return SimpleResponse.fail("ERROR");
            }
        }

        return SimpleResponse.success();
    }

    @Override
    public SimpleResponse markCurrencyAsDefault(String login, Currency currency) {
        User user = userRepository.getUserCurrencies(login);
        List<UserCurrency> currencies = user.getCurrencies();
        Optional<UserCurrency> oldDefaultCurrency = currencies.stream().filter(UserCurrency::isDefaultCurrency).findFirst();
        if (!oldDefaultCurrency.isPresent()) {
            LOG.error("There is no default currency for user " + login);
        }

        Optional<UserCurrency> defaultCurrency = currencies.stream().filter(userCurrency -> userCurrency.getName().equals(currency)).findFirst();
        if (!defaultCurrency.isPresent()) {
            LOG.error(StringUtils.join("User ", login, " doesn't have currency which is requested to be default ", currency));
            return SimpleResponse.fail("ERROR");
        }
        if (defaultCurrency.equals(oldDefaultCurrency)) {
            return SimpleResponse.success();
        }

        Query query = Query.query(Criteria.where("email").is(login));
        Update update = new Update().set(StringUtils.join("currencies.", currencies.indexOf(defaultCurrency.get()), ".defaultCurrency"), true);
        oldDefaultCurrency.ifPresent(userCurrency -> update.set(StringUtils.join("currencies.", currencies.indexOf(userCurrency), ".defaultCurrency"), false));
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        if (updateResult.getModifiedCount() != 1) {
            LOG.error("Error updating user profile - mark currency default. Number of updated items " + updateResult.getModifiedCount());
            return SimpleResponse.fail("ERROR");
        }

        return SimpleResponse.success();
    }

    @Override
    public SimpleResponse moveCurrency(String login, Currency currency, UpdateCurrencyRequest.Direction direction) {
        User user = userRepository.getUserCurrencies(login);
        List<UserCurrency> currencies = user.getCurrencies();
        Optional<UserCurrency> currencyItem = currencies.stream().filter(userCurrency -> userCurrency.getName().equals(currency)).findFirst();

        Optional<UserCurrency> secondCurrency;
        switch (direction) {
            case DOWN:
                secondCurrency = currencies.stream()
                        .filter(userCurrency -> userCurrency.getOrder() > currencyItem.get().getOrder())
                        .min(Comparator.comparingInt(UserCurrency::getOrder));
                break;
            case UP:
                secondCurrency = currencies.stream()
                        .filter(userCurrency -> userCurrency.getOrder() < currencyItem.get().getOrder())
                        .max(Comparator.comparingInt(UserCurrency::getOrder));
                break;
            default:
                secondCurrency = Optional.empty();
        }

        if (!secondCurrency.isPresent()) {
            return SimpleResponse.success();
        }

        Query query = Query.query(Criteria.where("email").is(login));
        Update update = new Update()
                .set(StringUtils.join("currencies.", currencies.indexOf(secondCurrency.get()), ".order"), currencyItem.map(UserCurrency::getOrder).get())
                .set(StringUtils.join("currencies.", currencies.indexOf(currencyItem.get()), ".order"), secondCurrency.map(UserCurrency::getOrder).get());
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        if (updateResult.getModifiedCount() != 1) {
            LOG.error("Error updating user profile - move currency. Number of updated items " + updateResult.getModifiedCount());
            return SimpleResponse.fail("ERROR");
        }

        return SimpleResponse.success();
    }

    @Override
    public SimpleResponse addAccount(String login, String title) {
        User user = userRepository.getUserAccounts(login);
        if (user.getAccounts().stream().anyMatch(userAccount -> StringUtils.equals(userAccount.getTitle(), title))) {
            return SimpleResponse.fail("ALREADY_EXIST");
        }

        int order = 1 + user.getAccounts().stream()
                .max(Comparator.comparingInt(Account::getOrder))
                .map(Account::getOrder)
                .orElse(0);
        Query query = Query.query(Criteria.where("email").is(login));
        Update update = new Update().addToSet("accounts", new Account(title, order));
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        if (updateResult.getModifiedCount() != 1) {
            LOG.error("Error updating user profile - add account. Number of updated items " + updateResult.getModifiedCount());
            return SimpleResponse.fail("ERROR");
        }

        return SimpleResponse.success();
    }
}