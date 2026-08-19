package com.inditex.prices.domain.exception;

import com.inditex.prices.domain.model.PriceQuery;

public class PriceNotFoundException extends RuntimeException {

    public PriceNotFoundException(PriceQuery query) {
        super("No hay tarifa aplicable para el producto %d de la cadena %d el %s".formatted(
                query.productId().value(), query.brandId().value(), query.applicationDate()));
    }
}
