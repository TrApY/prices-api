package com.inditex.prices.infrastructure.adapter.in.rest;

import com.inditex.prices.domain.model.Price;
import com.inditex.prices.infrastructure.adapter.in.rest.api.model.PriceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.Currency;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface PriceResponseMapper {

    @Mapping(target = "productId", source = "productId.value")
    @Mapping(target = "brandId", source = "brandId.value")
    @Mapping(target = "startDate", source = "validFrom")
    @Mapping(target = "endDate", source = "validTo")
    @Mapping(target = "price", source = "amount.amount")
    @Mapping(target = "currency", source = "amount.currency")
    PriceResponse toResponse(Price price);

    default String toCurrencyCode(Currency currency) {
        return currency.getCurrencyCode();
    }
}
