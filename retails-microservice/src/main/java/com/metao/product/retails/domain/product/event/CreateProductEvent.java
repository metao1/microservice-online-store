package com.metao.product.retails.domain.product.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.metao.ddd.base.DomainEvent;
import com.metao.product.retails.application.dto.ProductDTO;

import java.time.Instant;

public class CreateProductEvent implements DomainEvent {

    private final ProductDTO productDTO;
    private final Instant createdOn;
    private final Instant occurredOn;

    @JsonCreator
    public CreateProductEvent(ProductDTO productDTO, Instant createdOn, Instant occurredOn) {
        this.productDTO = productDTO;
        this.createdOn = createdOn;
        this.occurredOn = occurredOn;
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }

    Instant getCreatedOn() {
        return createdOn;
    }

    public ProductDTO getProductDTO() {
        return productDTO;
    }
}
