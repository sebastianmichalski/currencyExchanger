package com.currency.exchanger.configuration;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "configuration")
@AllArgsConstructor
@Getter
public class Configuration {

    private Double defaultSpread;
    private Map<String, Double> spreads;
    private String baseCurrency;
}