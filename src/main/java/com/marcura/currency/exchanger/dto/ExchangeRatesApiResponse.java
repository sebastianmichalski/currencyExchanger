package com.marcura.currency.exchanger.dto;

import java.math.BigDecimal;
import java.util.Map;

public record ExchangeRatesApiResponse(
    boolean success,
    long timestamp,
    String base,
    String date,
    Map<String, BigDecimal> rates) {

}
