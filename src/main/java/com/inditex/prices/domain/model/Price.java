package com.inditex.prices.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Tarifa de precio de un producto de una cadena, vigente en un rango de fechas.
 * Ambos extremos del rango son inclusivos.
 */
public record Price(
        ProductId productId,
        BrandId brandId,
        int priceList,
        LocalDateTime validFrom,
        LocalDateTime validTo,
        int priority,
        Money amount) {

    public Price {
        Objects.requireNonNull(productId, "El producto es obligatorio");
        Objects.requireNonNull(brandId, "La cadena es obligatoria");
        Objects.requireNonNull(validFrom, "El inicio de vigencia es obligatorio");
        Objects.requireNonNull(validTo, "El fin de vigencia es obligatorio");
        Objects.requireNonNull(amount, "El importe es obligatorio");
        if (priceList <= 0) {
            throw new IllegalArgumentException("La tarifa debe ser positiva: " + priceList);
        }
        if (priority < 0) {
            throw new IllegalArgumentException("La prioridad no puede ser negativa: " + priority);
        }
        if (validTo.isBefore(validFrom)) {
            throw new IllegalArgumentException(
                    "El fin de vigencia (" + validTo + ") no puede ser anterior al inicio (" + validFrom + ")");
        }
    }

    /** Indica si la tarifa está vigente en la fecha dada (extremos inclusivos). */
    public boolean isApplicableAt(LocalDateTime date) {
        return !date.isBefore(validFrom) && !date.isAfter(validTo);
    }
}
