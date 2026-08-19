package com.inditex.prices.application.service;

import com.inditex.prices.application.port.in.FindApplicablePriceUseCase;
import com.inditex.prices.application.port.out.PriceRepository;
import com.inditex.prices.domain.exception.PriceNotFoundException;
import com.inditex.prices.domain.model.Price;
import com.inditex.prices.domain.model.PriceQuery;
import com.inditex.prices.domain.model.PriceSelectionPolicy;

import java.util.Objects;

public class FindApplicablePriceService implements FindApplicablePriceUseCase {

    private final PriceRepository priceRepository;
    private final PriceSelectionPolicy selectionPolicy;

    public FindApplicablePriceService(
            PriceRepository priceRepository,
            PriceSelectionPolicy selectionPolicy) {
        this.priceRepository = Objects.requireNonNull(priceRepository);
        this.selectionPolicy = Objects.requireNonNull(selectionPolicy);
    }

    @Override
    public Price find(PriceQuery query) {
        return selectionPolicy.select(priceRepository.findCandidates(query))
                .orElseThrow(() -> new PriceNotFoundException(query));
    }
}
