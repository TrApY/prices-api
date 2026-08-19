package com.inditex.prices.infrastructure.adapter.out.persistence;

import com.inditex.prices.domain.model.BrandId;
import com.inditex.prices.domain.model.Money;
import com.inditex.prices.domain.model.Price;
import com.inditex.prices.domain.model.ProductId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.Currency;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface PriceEntityMapper {

    @Mapping(target = "amount", source = "entity")
    Price toDomain(PriceEntity entity);

    default Money toMoney(PriceEntity entity) {
        return new Money(entity.getAmount(), Currency.getInstance(entity.getCurrency()));
    }

    default ProductId toProductId(long value) {
        return new ProductId(value);
    }

    default BrandId toBrandId(long value) {
        return new BrandId(value);
    }
}
