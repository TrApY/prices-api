package com.inditex.prices.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HighestPriorityWinsTest {

    private final HighestPriorityWins policy = new HighestPriorityWins();

    @Test
    void picksTheHighestPriorityAmongOverlappingTariffs() {
        Price base = tariff(1, 0);
        Price promo = tariff(2, 1);

        Optional<Price> selected = policy.select(List.of(base, promo));

        assertEquals(promo, selected.orElseThrow());
    }

    @Test
    void breaksPriorityTiesDeterministicallyByHighestPriceList() {
        Price older = tariff(3, 1);
        Price newer = tariff(4, 1);

        assertEquals(newer, policy.select(List.of(newer, older)).orElseThrow());
        assertEquals(newer, policy.select(List.of(older, newer)).orElseThrow());
    }

    @Test
    void returnsTheOnlyCandidateWhenThereIsNoConflict() {
        Price only = tariff(1, 0);
        assertEquals(only, policy.select(List.of(only)).orElseThrow());
    }

    @Test
    void returnsEmptyWhenThereAreNoCandidates() {
        assertTrue(policy.select(List.of()).isEmpty());
    }

    private static Price tariff(int priceList, int priority) {
        return new Price(new ProductId(35455), new BrandId(1), priceList,
                LocalDateTime.parse("2020-06-14T00:00:00"), LocalDateTime.parse("2020-12-31T23:59:59"),
                priority, new Money(new BigDecimal("35.50"), Currency.getInstance("EUR")));
    }
}
