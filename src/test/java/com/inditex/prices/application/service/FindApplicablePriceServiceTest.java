package com.inditex.prices.application.service;

import com.inditex.prices.domain.exception.PriceNotFoundException;
import com.inditex.prices.domain.model.BrandId;
import com.inditex.prices.domain.model.HighestPriorityWins;
import com.inditex.prices.domain.model.Money;
import com.inditex.prices.domain.model.Price;
import com.inditex.prices.domain.model.PriceQuery;
import com.inditex.prices.domain.model.ProductId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FindApplicablePriceServiceTest {

    private static final PriceQuery QUERY = new PriceQuery(
            LocalDateTime.parse("2020-06-14T16:00:00"), new ProductId(35455), new BrandId(1));

    @Test
    void returnsTheWinningTariffAmongTheCandidates() {
        Price base = tariff(1, 0, "35.50");
        Price promo = tariff(2, 1, "25.45");
        FindApplicablePriceService service =
                new FindApplicablePriceService(query -> List.of(base, promo), new HighestPriorityWins());

        assertEquals(promo, service.find(QUERY));
    }

    @Test
    void failsWithPriceNotFoundWhenNoTariffApplies() {
        FindApplicablePriceService service =
                new FindApplicablePriceService(query -> List.of(), new HighestPriorityWins());

        PriceNotFoundException ex = assertThrows(PriceNotFoundException.class, () -> service.find(QUERY));

        assertTrue(ex.getMessage().contains("35455"));
        assertTrue(ex.getMessage().contains("2020-06-14T16:00"));
    }

    private static Price tariff(int priceList, int priority, String amount) {
        return new Price(new ProductId(35455), new BrandId(1), priceList,
                LocalDateTime.parse("2020-06-14T15:00:00"), LocalDateTime.parse("2020-06-14T18:30:00"),
                priority, new Money(new BigDecimal(amount), Currency.getInstance("EUR")));
    }
}
