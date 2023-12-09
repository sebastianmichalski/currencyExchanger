package com.marcura.currency.exchanger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.math.BigDecimal;

public record ExchangeRateResponse(
    @Schema(example = "EUR", requiredMode = RequiredMode.REQUIRED)
    String from,
    @Schema(example = "PLN", requiredMode = RequiredMode.REQUIRED)
    String to,
    @Schema(example = "4.2166442725", requiredMode = RequiredMode.REQUIRED)
    BigDecimal rate) {

}
