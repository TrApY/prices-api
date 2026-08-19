package com.inditex.prices.domain.model;

public record ProductId(long value) {

    public ProductId {
        if (value <= 0) {
            throw new IllegalArgumentException("El identificador de producto debe ser positivo: " + value);
        }
    }
}
