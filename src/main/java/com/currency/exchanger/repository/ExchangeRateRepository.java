package com.currency.exchanger.repository;

import com.currency.exchanger.entity.ExchangeRate;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    Optional<ExchangeRate> findByFromCurrencyAndToCurrencyAndDate(String from, String to, LocalDate date);

    Optional<ExchangeRate> findFirstByFromCurrencyAndToCurrencyOrderByDateDesc(String from, String to);
}