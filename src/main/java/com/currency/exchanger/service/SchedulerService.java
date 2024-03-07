package com.currency.exchanger.service;

import com.currency.exchanger.exceptions.FechingExchangeRatesException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final DataService dataService;

    // Scheduled task to fetch and store exchange rates daily at 12:05 AM GMT
    @Scheduled(cron = "0 5 0 * * ?", zone = "GMT")
    public synchronized void fetchAndStoreExchangeRates() throws FechingExchangeRatesException {
        log.info("Fetching exchange rates");
        dataService.fetchAndStoreData();
    }
}