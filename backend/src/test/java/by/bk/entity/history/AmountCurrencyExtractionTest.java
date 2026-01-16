package by.bk.entity.history;

import by.bk.entity.currency.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class AmountCurrencyExtractionTest {

    private static final Pattern AMOUNT_CURRENCY_PATTERN = Pattern.compile("-?\\s*(\\d+[,.]?\\d*)\\s*(BYN|USD|EUR|RUB)", Pattern.CASE_INSENSITIVE);

    @ParameterizedTest
    @CsvSource({
        "'7,14 USD bei DIGITALOCEAN.COM.', 7.14, USD",
        "'Karta 4.8431\nOplata\nUspeshno\nSumma:140.40 BYN\nOstatok:12239.51 BYN', 140.40, BYN",
        "'Карта 4.8431 Успешно Сумма: -52.91 BYN\nОстаток:12186.60 BYN', 52.91, BYN",
        "'Spisanie; 3.99 BYN; Mastercard#9761; BNB Bank>MINSK>BLR', 3.99, BYN",
        "'Oplata; 5.3 BYN; Mastercard#9761; SHOP', 5.3, BYN",
        "'Oplata; 1 BYN; Mastercard#9761; TRANSPORT', 1, BYN",
        "'Zachislenie; 1000 BYN; Mastercard#9761', 1000, BYN",
        "'Karta 4.8431\nPostuplenie\nUspeshno\nSumma:15217.29 BYN', 15217.29, BYN",
        "'Spisanie; 309 BYN; Mastercard#9761', 309, BYN"
    })
    void shouldExtractAmountAndCurrency(String message, double expectedAmount, String expectedCurrency) {
        var matcher = AMOUNT_CURRENCY_PATTERN.matcher(message);

        assertTrue(matcher.find(), "Pattern should match for: " + message);

        var amountStr = matcher.group(1).replace(",", ".");
        var currencyStr = matcher.group(2).toUpperCase();

        var actualAmount = Double.parseDouble(amountStr);
        var actualCurrency = Currency.valueOf(currencyStr);

        assertEquals(expectedAmount, actualAmount, 0.001, "Amount mismatch for: " + message);
        assertEquals(Currency.valueOf(expectedCurrency), actualCurrency, "Currency mismatch for: " + message);
    }

    @Test
    void shouldIgnoreMinusSign() {
        var message = "Сумма: -52.91 BYN";
        var matcher = AMOUNT_CURRENCY_PATTERN.matcher(message);

        assertTrue(matcher.find());
        var amountStr = matcher.group(1).replace(",", ".");
        var amount = Double.parseDouble(amountStr);

        assertEquals(52.91, amount, 0.001, "Minus sign should be ignored");
    }

    @Test
    void shouldHandleCommaAsDecimalSeparator() {
        var message = "7,14 USD bei DIGITALOCEAN.COM.";
        var matcher = AMOUNT_CURRENCY_PATTERN.matcher(message);

        assertTrue(matcher.find());
        var amountStr = matcher.group(1).replace(",", ".");
        var amount = Double.parseDouble(amountStr);

        assertEquals(7.14, amount, 0.001);
    }

    @Test
    void shouldMatchFirstAmountNotBalance() {
        var message = "Summa:140.40 BYN\nOstatok:12239.51 BYN";
        var matcher = AMOUNT_CURRENCY_PATTERN.matcher(message);

        assertTrue(matcher.find());
        var amountStr = matcher.group(1);

        assertEquals("140.40", amountStr, "Should match first amount (140.40), not balance (12239.51)");
    }

    @Test
    void shouldBeCaseInsensitive() {
        var message = "100 byn payment";
        var matcher = AMOUNT_CURRENCY_PATTERN.matcher(message);

        assertTrue(matcher.find());
        assertEquals("BYN", matcher.group(2).toUpperCase());
    }
}
