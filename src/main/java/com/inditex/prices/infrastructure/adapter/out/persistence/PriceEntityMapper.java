package com.inditex.prices.infrastructure.adapter.out.persistence;

import com.inditex.prices.domain.model.BrandId;
import com.inditex.prices.domain.model.Money;
import com.inditex.prices.domain.model.Price;
import com.inditex.prices.domain.model.ProductId;

import java.util.Currency;

final class PriceEntityMapper {

    private PriceEntityMapper() {
    }

    static Price toDomain(PriceEntity entity) {
        return new Price(
                new ProductId(entity.getProductId()),
                new BrandId(entity.getBrandId()),
                entity.getPriceList(),
                entity.getValidFrom(),
                entity.getValidTo(),
                entity.getPriority(),
                new Money(entity.getAmount(), Currency.getInstance(entity.getCurrency())));
    }
}
