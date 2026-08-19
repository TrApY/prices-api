package com.inditex.prices.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

public record PriceQuery(LocalDateTime applicationDate, ProductId productId, BrandId brandId) {

    public PriceQuery {
        Objects.requireNonNull(applicationDate, "La fecha de aplicación es obligatoria");
        Objects.requireNonNull(productId, "El producto es obligatorio");
        Objects.requireNonNull(brandId, "La cadena es obligatoria");
    }
}
