package com.inditex.prices.infrastructure.adapter.in.rest;

import com.inditex.prices.domain.model.Price;
import com.inditex.prices.infrastructure.adapter.in.rest.api.model.PriceResponse;

final class PriceResponseMapper {

    private PriceResponseMapper() {
    }

    static PriceResponse toResponse(Price price) {
        return new PriceResponse()
                .productId(price.productId().value())
                .brandId(price.brandId().value())
                .priceList(price.priceList())
                .startDate(price.validFrom())
                .endDate(price.validTo())
                .price(price.amount().amount())
                .currency(price.amount().currency().getCurrencyCode());
    }
}
