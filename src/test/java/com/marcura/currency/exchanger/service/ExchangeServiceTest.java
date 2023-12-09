package com.marcura.currency.exchanger.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.marcura.currency.exchanger.configuration.Configuration;
import com.marcura.currency.exchanger.entity.ExchangeRate;
import com.marcura.currency.exchanger.repository.ExchangeRateRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

class ExchangeServiceTest {

    private final Configuration configuration = new Configuration(
        6.0,
        Map.of("USD", 2.0, "PLN", 5.0),
        "EUR");
    private final ExchangeRateRepository exchangeRateRepository = mock(ExchangeRateRepository.class);

    private final ExchangeService exchangeService = new ExchangeService(configuration, exchangeRateRepository);

    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv")
    void shouldCalculateExchangeRates(String sourceCurrency,
                                      String targetCurrency,
                                      LocalDate date,
                                      BigDecimal expectedRate) {
        // Given
        mockDataForCurrenciesPairs(sourceCurrency, targetCurrency, date);

        // When
        var actual = exchangeService.calculateExchangeRate(sourceCurrency, targetCurrency, date);

        // Then
        if (actual == null) {
            assertNull(expectedRate);
        } else {
            assertEquals(expectedRate, actual.rate());
        }
    }

    private void mockDataForCurrenciesPairs(String sourceCurrency, String targetCurrency, LocalDate date) {
        doReturn(rates.stream()
            .filter(currenciesWithDateMatches(configuration.getBaseCurrency(), sourceCurrency, date))
            .findFirst())
            .when(exchangeRateRepository).findByFromCurrencyAndToCurrencyAndDate(configuration.getBaseCurrency(), sourceCurrency, date);
        doReturn(rates.stream()
            .filter(currenciesWithDateMatches(configuration.getBaseCurrency(), targetCurrency, date))
            .findFirst())
            .when(exchangeRateRepository).findByFromCurrencyAndToCurrencyAndDate(configuration.getBaseCurrency(), targetCurrency, date);
        doReturn(rates.stream()
            .filter(currenciesMatches(configuration.getBaseCurrency(), sourceCurrency))
            .max(Comparator.comparing(ExchangeRate::getDate)))
            .when(exchangeRateRepository).findFirstByFromCurrencyAndToCurrencyOrderByDateDesc(configuration.getBaseCurrency(), sourceCurrency);
        doReturn(rates.stream()
            .filter(currenciesMatches(configuration.getBaseCurrency(), targetCurrency))
            .max(Comparator.comparing(ExchangeRate::getDate)))
            .when(exchangeRateRepository).findFirstByFromCurrencyAndToCurrencyOrderByDateDesc(configuration.getBaseCurrency(), targetCurrency);
    }

    private static Predicate<ExchangeRate> currenciesMatches(String sourceCurrency, String targetCurrency) {
        return rate -> rate.getFromCurrency().equals(sourceCurrency)
            && rate.getToCurrency().equals(targetCurrency);
    }

    private static Predicate<ExchangeRate> currenciesWithDateMatches(String sourceCurrency, String targetCurrency, LocalDate date) {
        return rate -> rate.getFromCurrency().equals(sourceCurrency)
            && rate.getToCurrency().equals(targetCurrency)
            && rate.getDate().equals(date);
    }

    private static final Set<ExchangeRate> rates = Set.of(
        new ExchangeRate(0L, "EUR", "PLN", BigDecimal.valueOf(4.75432), LocalDate.parse("2022-01-01"), 0L),
        new ExchangeRate(1L, "EUR", "PLN", BigDecimal.valueOf(4.22045), LocalDate.parse("2023-01-01"), 0L),
        new ExchangeRate(2L, "EUR", "PLN", BigDecimal.valueOf(4.98742), LocalDate.parse("2021-01-01"), 0L),
        new ExchangeRate(3L, "EUR", "USD", BigDecimal.valueOf(0.82134), LocalDate.parse("2016-01-01"), 0L),
        new ExchangeRate(4L, "EUR", "AUD", BigDecimal.valueOf(1.64321), LocalDate.parse("2017-01-01"), 0L),
        new ExchangeRate(5L, "EUR", "TRY", BigDecimal.valueOf(31.2212), LocalDate.parse("2019-01-01"), 0L),
        new ExchangeRate(6L, "EUR", "USD", BigDecimal.valueOf(1.08087), LocalDate.parse("2021-01-01"), 0L)
    );
}