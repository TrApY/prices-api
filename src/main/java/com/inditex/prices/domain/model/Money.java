package com.inditex.prices.domain.model;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) {

    public Money {
        Objects.requireNonNull(amount, "El importe es obligatorio");
        Objects.requireNonNull(currency, "La divisa es obligatoria");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("El importe no puede ser negativo: " + amount);
        }
    }
}
