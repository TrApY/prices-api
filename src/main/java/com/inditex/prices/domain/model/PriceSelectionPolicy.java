package com.inditex.prices.domain.model;

import java.util.List;
import java.util.Optional;

public interface PriceSelectionPolicy {

    Optional<Price> select(List<Price> candidates);
}
