package com.marcura.currency.exchanger.service;

import com.marcura.currency.exchanger.dto.ExchangeRatesApiResponse;
import com.marcura.currency.exchanger.entity.ExchangeRate;
import com.marcura.currency.exchanger.exceptions.FechingExchangeRatesException;
import com.marcura.currency.exchanger.repository.ExchangeRateRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataService {

    @Value("${api-base-url}")
    private String API_BASE_URL;
    @Value("${api-key}")
    private String API_KEY;
    private final RestTemplate restTemplate;

    private final ExchangeRateRepository exchangeRateRepository;

    @Retryable
    @Transactional
    public void fetchAndStoreData() throws FechingExchangeRatesException {
        ExchangeRatesApiResponse apiResponse = fetchExchangeRates();
        if (apiResponse != null && apiResponse.success()) {
            storeExchangeRates(apiResponse);
        } else {
            log.error("Failed to retrieve exchange rates: {}", apiResponse);
            throw new FechingExchangeRatesException();
        }
    }

    private ExchangeRatesApiResponse fetchExchangeRates() {
        log.info("Fetching data from {}", API_BASE_URL);
        var apiUrl = String.format("%s/latest?access_key=%s", API_BASE_URL, API_KEY);
        return restTemplate.getForObject(apiUrl, ExchangeRatesApiResponse.class);
    }

    private void storeExchangeRates(ExchangeRatesApiResponse apiResponse) {
        log.info("Storing exchange rates");
        for (Map.Entry<String, BigDecimal> entry : apiResponse.rates().entrySet()) {
            var fromCurrency = apiResponse.base();
            var toCurrency = entry.getKey();
            var currentDate = LocalDate.parse(apiResponse.date());

            ExchangeRate existingRate = exchangeRateRepository
                .findByFromCurrencyAndToCurrencyAndDate(fromCurrency, toCurrency, currentDate)
                .orElse(null);

            if (existingRate != null) {
                // Update existing entry, to not duplicate records in table and not too loose counter value
                log.info("Updating record {} -> {} on date {} with rate {}",
                    fromCurrency, toCurrency, currentDate, entry.getValue());
                existingRate.setRate(entry.getValue());
                exchangeRateRepository.save(existingRate);
            } else {
                log.info("Inserting new record {} -> {} on date {} with rate {}",
                    fromCurrency, toCurrency, currentDate, entry.getValue());
                var exchangeRate = ExchangeRate.builder()
                    .fromCurrency(apiResponse.base())
                    .toCurrency(toCurrency)
                    .rate(entry.getValue())
                    .date(LocalDate.parse(apiResponse.date()))
                    .counter(0L)
                    .build();

                exchangeRateRepository.save(exchangeRate);
            }
        }
    }
}
