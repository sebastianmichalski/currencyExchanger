package com.currency.exchanger;

import com.currency.exchanger.configuration.Configuration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableConfigurationProperties(Configuration.class)
@EnableRetry
public class CurrencyExchangerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CurrencyExchangerApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
