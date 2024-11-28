package by.bk.entity.user;

import by.bk.controller.model.response.SimpleResponse;
import by.bk.entity.budget.BudgetAPI;
import by.bk.entity.currency.Currency;
import by.bk.entity.history.HistoryAPI;
import by.bk.entity.user.model.SubAccount;
import by.bk.entity.user.model.SubCategoryType;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserFacade {

  private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("##0.00", DecimalFormatSymbols.getInstance(Locale.US));

  private final UserAPI userAPI;
  private final HistoryAPI historyAPI;
  private final BudgetAPI budgetAPI;

  public SimpleResponse deleteAccount(String login, String title) {
    var maybeAccount = userAPI.deleteAccount(login, title);
    if (maybeAccount.isEmpty()) {
      return SimpleResponse.fail();
    }

    maybeAccount.get().getSubAccounts().forEach(subAccount -> subAccount.getBalance().forEach((currency, value) -> {
      historyAPI.addBalanceHistoryItem(login, currency, title, subAccount.getTitle(), () -> value * -1);
    }));

    return SimpleResponse.success();
  }

  public SimpleResponse addSubAccount(String login, String subAccountTitle, String accountTitle, String icon, Map<Currency, Double> balance) {
    var response = userAPI.addSubAccount(login, subAccountTitle, accountTitle, icon, balance);
    if (response.isSuccess()) {
      balance.forEach((currency, value) -> {
        historyAPI.addBalanceHistoryItem(login, currency, accountTitle, subAccountTitle, () -> value);
      });
    }

    return response;
  }

  public SimpleResponse deleteSubAccount(String login, String accountTitle, String subAccountTitle) {
    var maybeSubAccount = userAPI.deleteSubAccount(login, accountTitle, subAccountTitle);
    if (maybeSubAccount.isEmpty()) {
      return SimpleResponse.fail();
    }

    maybeSubAccount.get().getBalance().forEach((currency, value) -> historyAPI.addBalanceHistoryItem(login, currency, accountTitle, subAccountTitle, () -> value * -1));
    return SimpleResponse.success();
  }

  public SimpleResponse moveSubCategoryToAnotherCategory(String login, String oldCategoryTitle, String newCategoryTitle, String subCategoryTitle, SubCategoryType subCategoryType) {
    var response = userAPI.moveSubCategoryToAnotherCategory(login, oldCategoryTitle, newCategoryTitle, subCategoryTitle, subCategoryType);
    if (response.isSuccess()) {
      var historyItems = historyAPI.getSuitable(login, oldCategoryTitle, subCategoryTitle, subCategoryType);
      response = budgetAPI.moveCategory(login, oldCategoryTitle, newCategoryTitle, subCategoryTitle, subCategoryType, historyItems);
    }

    return response;
  }

  public SimpleResponse changeSubAccountBalance(String login, String subAccountTitle, String accountTitle, Map<Currency, Double> balance) {
    var maybeSubAccount = userAPI.changeSubAccountBalance(login, subAccountTitle, accountTitle, balance);
    if (maybeSubAccount.isEmpty()) {
      return SimpleResponse.fail();
    }

    addBalanceHistoryItemOnBalanceEdit(login, accountTitle, subAccountTitle, maybeSubAccount.get(), balance);
    return SimpleResponse.success();
  }

  public SimpleResponse editSubAccount(String login, String accountTitle, String oldSubAccountTitle, String newSubAccountTitle, String icon, Map<Currency, Double> balance) {
    var maybeSubAccount = userAPI.editSubAccount(login, accountTitle, oldSubAccountTitle, newSubAccountTitle, icon, balance);
    if (maybeSubAccount.isEmpty()) {
      return SimpleResponse.fail();
    }

    addBalanceHistoryItemOnBalanceEdit(login, accountTitle, newSubAccountTitle, maybeSubAccount.get(), balance);
    return SimpleResponse.success();
  }

  private void addBalanceHistoryItemOnBalanceEdit(String login, String accountTitle, String subAccountTitle, SubAccount subAccount, Map<Currency, Double> balance) {
    subAccount.getBalance().forEach((currency, value) -> {
      var currencyValue = !balance.containsKey(currency) ? value * -1 : Double.parseDouble(CURRENCY_FORMAT.format(balance.get(currency) - value));
      if (currencyValue != 0) {
        historyAPI.addBalanceHistoryItem(login, currency, accountTitle, subAccountTitle, () -> currencyValue);
      }
    });

    CollectionUtils.disjunction(subAccount.getBalance().keySet(), balance.keySet()).forEach(currency -> {
      if (balance.containsKey(currency)) {
        historyAPI.addBalanceHistoryItem(login, currency, accountTitle, subAccountTitle, () -> balance.get(currency));
      }
    });
  }
}
