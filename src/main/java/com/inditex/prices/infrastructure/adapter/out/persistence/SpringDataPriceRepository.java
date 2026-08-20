package com.inditex.prices.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

interface SpringDataPriceRepository extends JpaRepository<PriceEntity, UUID> {

    @Query("""
            select p from PriceEntity p
            where p.productId = :productId
              and p.brandId = :brandId
              and p.validFrom <= :date
              and p.validTo >= :date
            """)
    List<PriceEntity> findInForceAt(
            @Param("productId") long productId,
            @Param("brandId") long brandId,
            @Param("date") LocalDateTime date);
}
