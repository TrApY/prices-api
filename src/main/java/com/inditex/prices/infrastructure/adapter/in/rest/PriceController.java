package com.inditex.prices.infrastructure.adapter.in.rest;

import com.inditex.prices.application.port.in.FindApplicablePriceUseCase;
import com.inditex.prices.domain.model.BrandId;
import com.inditex.prices.domain.model.PriceQuery;
import com.inditex.prices.domain.model.ProductId;
import com.inditex.prices.infrastructure.adapter.in.rest.api.PricesApi;
import com.inditex.prices.infrastructure.adapter.in.rest.api.model.PriceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
class PriceController implements PricesApi {

    private final FindApplicablePriceUseCase findApplicablePrice;

    @Override
    public ResponseEntity<PriceResponse> findApplicablePrice(
            LocalDateTime applicationDate, Long productId, Long brandId) {
        PriceQuery query = new PriceQuery(applicationDate, new ProductId(productId), new BrandId(brandId));
        return ResponseEntity.ok(PriceResponseMapper.toResponse(findApplicablePrice.find(query)));
    }
}
