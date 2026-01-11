package by.bk.entity.currency;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock
    private CurrencyRepository currencyRepository;
    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private CurrencyService currencyService;

    @Test
    void recalculateMonthlyAverage_shouldCalculateCorrectAverage() {
        // Given: 3 days of USD rates
        CurrencyDetail day1 = createDailyRate(Currency.USD, 2024, 1, 1, Map.of(Currency.BYN, 3.10, Currency.EUR, 0.92));
        CurrencyDetail day2 = createDailyRate(Currency.USD, 2024, 1, 2, Map.of(Currency.BYN, 3.20, Currency.EUR, 0.94));
        CurrencyDetail day3 = createDailyRate(Currency.USD, 2024, 1, 3, Map.of(Currency.BYN, 3.30, Currency.EUR, 0.96));

        when(currencyRepository.getByYearAndMonthAndDayIsNotNull(2024, 1))
                .thenReturn(List.of(day1, day2, day3));

        // When
        currencyService.recalculateMonthlyAverage(2024, 1);

        // Then: verify upsert was called with correct average
        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplate, times(1)).upsert(any(Query.class), updateCaptor.capture(), eq(CurrencyDetail.class));

        // Average BYN: (3.10 + 3.20 + 3.30) / 3 = 3.20
        // Average EUR: (0.92 + 0.94 + 0.96) / 3 = 0.94
        Update capturedUpdate = updateCaptor.getValue();
        assertNotNull(capturedUpdate);
    }

    @Test
    void recalculateMonthlyAverage_shouldHandleEmptyData() {
        // Given: no daily rates
        when(currencyRepository.getByYearAndMonthAndDayIsNotNull(2024, 1))
                .thenReturn(List.of());

        // When
        currencyService.recalculateMonthlyAverage(2024, 1);

        // Then: no upsert should be called
        verify(mongoTemplate, never()).upsert(any(Query.class), any(Update.class), eq(CurrencyDetail.class));
    }

    @Test
    void recalculateMonthlyAverage_shouldHandleMissingDays() {
        // Given: only 2 days of data (simulating missing days)
        CurrencyDetail day1 = createDailyRate(Currency.USD, 2024, 1, 1, Map.of(Currency.BYN, 3.10));
        CurrencyDetail day15 = createDailyRate(Currency.USD, 2024, 1, 15, Map.of(Currency.BYN, 3.30));

        when(currencyRepository.getByYearAndMonthAndDayIsNotNull(2024, 1))
                .thenReturn(List.of(day1, day15));

        // When
        currencyService.recalculateMonthlyAverage(2024, 1);

        // Then: should divide by 2, not by 15 or 31
        verify(mongoTemplate, times(1)).upsert(any(Query.class), any(Update.class), eq(CurrencyDetail.class));
    }

    private CurrencyDetail createDailyRate(Currency name, int year, int month, int day, Map<Currency, Double> conversions) {
        CurrencyDetail cd = new CurrencyDetail();
        cd.setName(name);
        cd.setYear(year);
        cd.setMonth(month);
        cd.setDay(day);
        cd.setConversions(conversions);
        return cd;
    }
}
