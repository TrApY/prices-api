package com.inditex.prices.infrastructure.adapter.out.persistence;

import com.inditex.prices.domain.model.BrandId;
import com.inditex.prices.domain.model.Price;
import com.inditex.prices.domain.model.PriceQuery;
import com.inditex.prices.domain.model.ProductId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Mismo adaptador y mismas migraciones contra un PostgreSQL real: el puerto de
 * persistencia es agnóstico del motor (en runtime la app usa H2 por requisito).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@Import({PricePersistenceAdapter.class, PriceEntityMapperImpl.class})
class PricePersistenceAdapterPostgresTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));

    @Autowired
    private PricePersistenceAdapter adapter;

    @Test
    void behavesExactlyLikeH2ForTheCandidateQuery() {
        List<Price> candidates = adapter.findCandidates(queryAt("2020-06-14T16:00:00"));

        assertEquals(2, candidates.size());
        assertEquals(2, adapter.findCandidates(queryAt("2020-06-14T18:30:00")).size());
        assertTrue(adapter.findCandidates(queryAt("2019-01-01T00:00:00")).isEmpty());
    }

    @Test
    void mapsAmountsWithTheSameScaleAsH2() {
        Price promo = adapter.findCandidates(queryAt("2020-06-14T16:00:00")).stream()
                .filter(p -> p.priceList() == 2)
                .findFirst()
                .orElseThrow();

        assertEquals(new BigDecimal("25.45"), promo.amount().amount());
    }

    private static PriceQuery queryAt(String date) {
        return new PriceQuery(LocalDateTime.parse(date), new ProductId(35455), new BrandId(1));
    }
}
