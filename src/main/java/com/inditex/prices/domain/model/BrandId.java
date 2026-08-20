package com.inditex.prices.domain.model;

public record BrandId(long value) {

    public BrandId {
        if (value <= 0) {
            throw new IllegalArgumentException("El identificador de cadena debe ser positivo: " + value);
        }
    }
}
