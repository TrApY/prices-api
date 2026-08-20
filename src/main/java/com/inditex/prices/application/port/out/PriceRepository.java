package com.inditex.prices.application.port.out;

import com.inditex.prices.domain.model.Price;
import com.inditex.prices.domain.model.PriceQuery;

import java.util.List;

/**
 * Puerto de salida: acceso a las tarifas.
 * El adaptador devuelve solo las tarifas del producto y cadena vigentes en la fecha
 * de la consulta; la elección entre ellas es regla de dominio.
 */
public interface PriceRepository {

    List<Price> findCandidates(PriceQuery query);
}
