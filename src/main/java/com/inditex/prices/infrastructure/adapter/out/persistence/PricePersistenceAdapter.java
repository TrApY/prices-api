package com.inditex.prices.infrastructure.adapter.out.persistence;

import com.inditex.prices.application.port.out.PriceRepository;
import com.inditex.prices.domain.model.Price;
import com.inditex.prices.domain.model.PriceQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
class PricePersistenceAdapter implements PriceRepository {

    private final SpringDataPriceRepository springDataRepository;

    @Override
    public List<Price> findCandidates(PriceQuery query) {
        return springDataRepository
                .findInForceAt(query.productId().value(), query.brandId().value(), query.applicationDate())
                .stream()
                .map(PriceEntityMapper::toDomain)
                .toList();
    }
}
