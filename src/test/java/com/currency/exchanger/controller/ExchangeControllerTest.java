package com.currency.exchanger.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.currency.exchanger.dto.ExchangeRateResponse;
import com.currency.exchanger.exceptions.FechingExchangeRatesException;
import com.currency.exchanger.service.ExchangeService;
import com.currency.exchanger.service.SchedulerService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootTest
class ExchangeControllerTest {

    private static final String PATH = "/exchange";
    private static final String HTTP_URL = "http://foo.bar";
    public final ExchangeRateResponse exchangeRateResponse = mock(ExchangeRateResponse.class);
    private final ExchangeService exchangeService = mock(ExchangeService.class);
    private final SchedulerService schedulerService = mock(SchedulerService.class);
    private final ExchangeController exchangeController = new ExchangeController(exchangeService, schedulerService);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(exchangeController).build();

    @Test
    void shouldReturnNotFoundWhenResponseFromServiceIsNull() throws Exception {
        when(exchangeService.calculateExchangeRate(any(), any(), any()))
            .thenReturn(null);

        mockMvc.perform(
                MockMvcRequestBuilders.get(UriComponentsBuilder.fromHttpUrl(HTTP_URL)
                    .path(PATH)
                    .queryParam("from", "EUR")
                    .queryParam("to", "PLN")
                    .build()
                    .toUri()))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andReturn();
    }

    @Test
    void shouldReturnOkWhenResponseFromServiceIsNotNull() throws Exception {
        when(exchangeService.calculateExchangeRate(any(), any(), any()))
            .thenReturn(exchangeRateResponse);

        mockMvc.perform(
                MockMvcRequestBuilders.get(UriComponentsBuilder.fromHttpUrl(HTTP_URL)
                    .path(PATH)
                    .queryParam("from", "EUR")
                    .queryParam("to", "PLN")
                    .build()
                    .toUri()))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();
    }

    @Test
    void shouldReturnInternalServerErrorWhenExceptionIsThrownDuringExchangeRatesUpdate() throws Exception {
        doThrow(FechingExchangeRatesException.class).when(schedulerService).fetchAndStoreExchangeRates();

        mockMvc.perform(
                MockMvcRequestBuilders.put(UriComponentsBuilder.fromHttpUrl(HTTP_URL)
                    .path(PATH)
                    .build()
                    .toUri()))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andReturn();
    }

    @Test
    void shouldReturnOkWhenNoExceptionIsThrownDuringExchangeRatesUpdate() throws Exception {
        doNothing().when(schedulerService).fetchAndStoreExchangeRates();

        mockMvc.perform(
                MockMvcRequestBuilders.put(UriComponentsBuilder.fromHttpUrl(HTTP_URL)
                    .path(PATH)
                    .build()
                    .toUri()))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();
    }
}