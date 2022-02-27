package com.metao.product.domain.event;

import java.time.Instant;

import com.metao.ddd.base.DomainEvent;
import com.metao.product.application.dto.ProductDTO;

import org.springframework.lang.NonNull;

public record CreateProductEvent(@NonNull ProductDTO productDTO, @NonNull Instant createdOn, @NonNull Instant  occurredOn ) implements DomainEvent {

    public static CreateProductEvent createEvent(ProductDTO productDTO) {
            return new CreateProductEvent(productDTO, Instant.now(), Instant.now());
    }
}
