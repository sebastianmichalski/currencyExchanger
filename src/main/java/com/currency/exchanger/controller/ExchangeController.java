package com.currency.exchanger.controller;

import com.currency.exchanger.dto.ExchangeRateResponse;
import com.currency.exchanger.exceptions.FechingExchangeRatesException;
import com.currency.exchanger.service.ExchangeService;
import com.currency.exchanger.service.SchedulerService;
import com.currency.exchanger.validator.CurrencyConstraint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/exchange")
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeService exchangeService;
    private final SchedulerService schedulerService;

    @Operation(summary = "Calculate exchange rates between currencies")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Calculation done"),
        @ApiResponse(responseCode = "404", description = "Exchange rate cannot be retrieved",
            content = @Content(schema = @Schema(hidden = true, example = "{}")))
    })
    @GetMapping
    public ResponseEntity<ExchangeRateResponse> calculateExchangeRate(
        @Parameter(example = "EUR", required = true, description = "Currency code which we want to exchange from")
        @RequestParam @CurrencyConstraint String from,
        @Parameter(example = "PLN", required = true, description = "Currency code which we want to exchange to")
        @RequestParam @CurrencyConstraint String to,
        @Parameter(example = "2023-10-01", description = "Date of exchange rate")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("Calculate exchange rate between {} and {}. Date: {}", from, to, date == null ? "latest" : date);
        var exchangeRateResponse = exchangeService.calculateExchangeRate(from, to, date);

        if (exchangeRateResponse != null) {
            return ResponseEntity.ok(exchangeRateResponse);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Update exchanges rates")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated"),
        @ApiResponse(responseCode = "500", description = "Failed to update exchangeRates"),
    })
    @PutMapping
    public ResponseEntity<String> updateExchangeRates() {
        try {
            log.info("Updating exchange rates");
            schedulerService.fetchAndStoreExchangeRates();
            return ResponseEntity.ok("Exchange rates updated");
        } catch (FechingExchangeRatesException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}