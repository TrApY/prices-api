package com.inditex.prices.domain.model;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Regla del negocio: entre tarifas que coinciden en fechas, aplica la de mayor prioridad.
 * Desempate determinista: si dos tarifas comparten prioridad, gana la de mayor
 * identificador de tarifa (la más reciente en la práctica).
 */
public final class HighestPriorityWins implements PriceSelectionPolicy {

    private static final Comparator<Price> BY_PRIORITY_THEN_PRICE_LIST =
            Comparator.comparingInt(Price::priority).thenComparingInt(Price::priceList);

    @Override
    public Optional<Price> select(List<Price> candidates) {
        return candidates.stream().max(BY_PRIORITY_THEN_PRICE_LIST);
    }
}
