package com.inditex.prices.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PriceTest {

    private static final LocalDateTime FROM = LocalDateTime.parse("2020-06-14T15:00:00");
    private static final LocalDateTime TO = LocalDateTime.parse("2020-06-14T18:30:00");

    @Test
    void isApplicableWithinValidityRange() {
        assertTrue(price(FROM, TO).isApplicableAt(LocalDateTime.parse("2020-06-14T16:00:00")));
    }

    @Test
    void validityBoundsAreInclusiveOnBothEnds() {
        Price price = price(FROM, TO);
        assertTrue(price.isApplicableAt(FROM));
        assertTrue(price.isApplicableAt(TO));
    }

    @Test
    void isNotApplicableOutsideValidityRange() {
        Price price = price(FROM, TO);
        assertFalse(price.isApplicableAt(FROM.minusSeconds(1)));
        assertFalse(price.isApplicableAt(TO.plusSeconds(1)));
    }

    @Test
    void rejectsValidityEndingBeforeItStarts() {
        assertThrows(IllegalArgumentException.class, () -> price(TO, FROM));
    }

    @Test
    void rejectsNegativeAmounts() {
        assertThrows(IllegalArgumentException.class,
                () -> new Money(new BigDecimal("-0.01"), Currency.getInstance("EUR")));
    }

    @Test
    void rejectsNonPositiveIdentifiers() {
        assertThrows(IllegalArgumentException.class, () -> new ProductId(0));
        assertThrows(IllegalArgumentException.class, () -> new BrandId(-1));
    }

    private static Price price(LocalDateTime from, LocalDateTime to) {
        return new Price(new ProductId(35455), new BrandId(1), 2, from, to, 1,
                new Money(new BigDecimal("25.45"), Currency.getInstance("EUR")));
    }
}
