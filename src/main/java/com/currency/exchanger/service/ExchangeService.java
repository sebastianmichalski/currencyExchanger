package com.currency.exchanger.service;

import com.currency.exchanger.configuration.Configuration;
import com.currency.exchanger.dto.ExchangeRateResponse;
import com.currency.exchanger.entity.ExchangeRate;
import com.currency.exchanger.repository.ExchangeRateRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class ExchangeService {

    private final Configuration configuration;
    private final ExchangeRateRepository exchangeRateRepository;
    private static final MathContext mathContext = new MathContext(20);

    @Transactional
    public ExchangeRateResponse calculateExchangeRate(String sourceCurrency, String targetCurrency, LocalDate date) {
        var exchangeRateBaseToSource = getExchangeRate(configuration.getBaseCurrency(), sourceCurrency, date);
        var exchangeRateBaseToTarget = getExchangeRate(configuration.getBaseCurrency(), targetCurrency, date);

        if (exchangeRateBaseToTarget != null && exchangeRateBaseToSource != null) {
            log.info("Calculate exchange rate between {} and {}. Date: {}", sourceCurrency, targetCurrency, date == null ? "latest" : date);
            var rateWithSpread = getRateWithSpread(sourceCurrency, targetCurrency, exchangeRateBaseToTarget, exchangeRateBaseToSource);
            return new ExchangeRateResponse(sourceCurrency, targetCurrency, rateWithSpread);
        } else {
            log.error("One of currencies without exchange rates from base currency: sourceCurrency -> {}, targetCurrency -> {}",
                exchangeRateBaseToSource, exchangeRateBaseToTarget);
            return null;
        }
    }

    private BigDecimal getRateWithSpread(String sourceCurrency, String targetCurrency, BigDecimal exchangeRateBaseToTarget, BigDecimal exchangeRateBaseToSource) {
        var spreadFrom = getSpread(sourceCurrency);
        var spreadTo = getSpread(targetCurrency);
        return exchangeRateBaseToTarget.divide(exchangeRateBaseToSource, mathContext)
            .multiply(BigDecimal.ONE.subtract(
                spreadTo.max(spreadFrom).divide(BigDecimal.valueOf(100), mathContext)));
    }

    private BigDecimal getSpread(String currencyCode) {
        log.info("Calculating spread");
        if (currencyCode.equals(configuration.getBaseCurrency())) {
            log.info("Spread 0.0%, base currency");
            return BigDecimal.ZERO;
        } else {
            var spreads = configuration.getSpreads();
            if (spreads.containsKey(currencyCode)) {
                log.info("Spread fixed in static map");
                return BigDecimal.valueOf(spreads.get(currencyCode));
            } else {
                log.info("Spread default");
                return BigDecimal.valueOf(configuration.getDefaultSpread());
            }
        }
    }

    private BigDecimal getExchangeRate(String fromCurrency, String toCurrency, LocalDate date) {
        if (date == null) {
            log.info("Retrieving exchange rate: from {}, to {}. Latest date", fromCurrency, toCurrency);
            var exchangeRate = exchangeRateRepository.findFirstByFromCurrencyAndToCurrencyOrderByDateDesc(fromCurrency, toCurrency);
            exchangeRate.ifPresent(this::incrementCounter);

            return exchangeRate
                .map(ExchangeRate::getRate)
                .orElse(null);
        }

        log.info("Retrieving exchange rate: from {}, to {}. Date: {}", fromCurrency, toCurrency, date);
        var exchangeRate = exchangeRateRepository.findByFromCurrencyAndToCurrencyAndDate(fromCurrency, toCurrency, date);
        exchangeRate.ifPresent(this::incrementCounter);

        return exchangeRate
            .map(ExchangeRate::getRate)
            .orElse(null);
    }

    private void incrementCounter(ExchangeRate exchangeRate) {
        log.info("Incrementing requests counter: from {}, to {}. Date: {}. Current counter: {}",
            exchangeRate.getFromCurrency(), exchangeRate.getToCurrency(), exchangeRate.getDate(), exchangeRate.getCounter());
        exchangeRate.incrementCounter();
        exchangeRateRepository.save(exchangeRate);
    }
}
