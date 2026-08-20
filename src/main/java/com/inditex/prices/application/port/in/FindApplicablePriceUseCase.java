package com.inditex.prices.application.port.in;

import com.inditex.prices.domain.model.Price;
import com.inditex.prices.domain.model.PriceQuery;

/** Puerto de entrada: consulta del precio aplicable a un producto de una cadena en una fecha. */
public interface FindApplicablePriceUseCase {

    /**
     * @throws com.inditex.prices.domain.exception.PriceNotFoundException si ninguna tarifa aplica
     */
    Price find(PriceQuery query);
}
