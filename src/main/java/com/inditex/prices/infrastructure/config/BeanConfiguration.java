package com.inditex.prices.infrastructure.config;

import com.inditex.prices.application.port.in.FindApplicablePriceUseCase;
import com.inditex.prices.application.port.out.PriceRepository;
import com.inditex.prices.application.service.FindApplicablePriceService;
import com.inditex.prices.domain.model.HighestPriorityWins;
import com.inditex.prices.domain.model.PriceSelectionPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wiring explícito: dominio y aplicación quedan libres de anotaciones del framework. */
@Configuration
class BeanConfiguration {

    @Bean
    PriceSelectionPolicy priceSelectionPolicy() {
        return new HighestPriorityWins();
    }

    @Bean
    FindApplicablePriceUseCase findApplicablePriceUseCase(
            PriceRepository priceRepository, PriceSelectionPolicy priceSelectionPolicy) {
        return new FindApplicablePriceService(priceRepository, priceSelectionPolicy);
    }
}
