package com.inditex.prices.infrastructure.adapter.out.persistence;

import com.inditex.prices.domain.model.BrandId;
import com.inditex.prices.domain.model.Price;
import com.inditex.prices.domain.model.PriceQuery;
import com.inditex.prices.domain.model.ProductId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import({PricePersistenceAdapter.class, PriceEntityMapperImpl.class})
class PricePersistenceAdapterTest {

    @Autowired
    private PricePersistenceAdapter adapter;

    @Test
    void returnsOnlyTheTariffsInForceAtTheQueryDate() {
        List<Price> candidates = adapter.findCandidates(queryAt("2020-06-14T16:00:00"));

        assertEquals(2, candidates.size());
        assertTrue(candidates.stream().anyMatch(p -> p.priceList() == 1));
        assertTrue(candidates.stream().anyMatch(p -> p.priceList() == 2));
    }

    @Test
    void validityBoundsAreInclusiveInTheQuery() {
        assertEquals(2, adapter.findCandidates(queryAt("2020-06-14T18:30:00")).size());
        assertEquals(1, adapter.findCandidates(queryAt("2020-06-14T18:30:01")).size());
    }

    @Test
    void returnsEmptyWhenNoTariffApplies() {
        assertTrue(adapter.findCandidates(queryAt("2019-01-01T00:00:00")).isEmpty());
    }

    @Test
    void mapsTheEntityIntoTheDomainModel() {
        Price promo = adapter.findCandidates(queryAt("2020-06-14T16:00:00")).stream()
                .filter(p -> p.priceList() == 2)
                .findFirst()
                .orElseThrow();

        assertEquals(35455, promo.productId().value());
        assertEquals(1, promo.brandId().value());
        assertEquals(1, promo.priority());
        assertEquals(new BigDecimal("25.45"), promo.amount().amount());
        assertEquals(Currency.getInstance("EUR"), promo.amount().currency());
        assertEquals(LocalDateTime.parse("2020-06-14T15:00:00"), promo.validFrom());
        assertEquals(LocalDateTime.parse("2020-06-14T18:30:00"), promo.validTo());
    }

    private static PriceQuery queryAt(String date) {
        return new PriceQuery(LocalDateTime.parse(date), new ProductId(35455), new BrandId(1));
    }
}
