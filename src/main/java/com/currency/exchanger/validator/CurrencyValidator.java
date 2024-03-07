package com.currency.exchanger.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;

public class CurrencyValidator implements ConstraintValidator<CurrencyConstraint, String> {

    private final Set<String> CURRENCY_CODES = Currency.getAvailableCurrencies()
        .stream()
        .map(Currency::getCurrencyCode)
        .collect(Collectors.toSet());

    @Override
    public void initialize(CurrencyConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String currency, ConstraintValidatorContext constraintValidatorContext) {
        return CURRENCY_CODES.contains(currency);
    }
}
