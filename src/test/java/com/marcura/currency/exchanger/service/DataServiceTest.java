package com.marcura.currency.exchanger.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.marcura.currency.exchanger.dto.ExchangeRatesApiResponse;
import com.marcura.currency.exchanger.exceptions.FechingExchangeRatesException;
import com.marcura.currency.exchanger.repository.ExchangeRateRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class DataServiceTest {

    private final ExchangeRateRepository exchangeRateRepository = mock(ExchangeRateRepository.class);
    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final ExchangeRatesApiResponse apiResponse = mock(ExchangeRatesApiResponse.class);
    private final DataService dataService = spy(new DataService(restTemplate, exchangeRateRepository));

    @Test
    void shouldThrowExceptionWhenResponseFromExternalApiIsNotSuccessful() {
        Assertions.assertThrows(FechingExchangeRatesException.class, () -> {
            when(apiResponse.success()).thenReturn(false);
            when(restTemplate.getForObject(anyString(), eq(ExchangeRatesApiResponse.class)))
                .thenReturn(apiResponse);

            dataService.fetchAndStoreData();
        });
    }

    @Test
    void shouldThrowExceptionWhenResponseFromExternalApiIsNull() {
        Assertions.assertThrows(FechingExchangeRatesException.class, () -> {
            when(restTemplate.getForObject(anyString(), eq(ExchangeRatesApiResponse.class)))
                .thenReturn(null);

            dataService.fetchAndStoreData();
        });
    }

    @Test
    void shouldProceedWhenResponseFromExternalApiIsSuccessful() {
        // Given
        when(apiResponse.success()).thenReturn(true);
        when(restTemplate.getForObject(anyString(), eq(ExchangeRatesApiResponse.class)))
            .thenReturn(apiResponse);

        // Expect
        assertDoesNotThrow(dataService::fetchAndStoreData);
    }
}