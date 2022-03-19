package com.metao.book.retails.domain.event;

import java.time.Instant;

import com.metao.book.retails.application.dto.ProductDTO;
import com.metao.book.shared.domain.base.DomainEvent;

import org.springframework.context.ApplicationEvent;
import org.springframework.lang.NonNull;

public class CreateProductEvent extends ApplicationEvent implements DomainEvent {
    private final ProductDTO productDTO;
    private final Instant createdOn;
    private final Instant occurredOn;

    public CreateProductEvent(
            @NonNull final ProductDTO productDTO,
            @NonNull final Instant createdOn,
            @NonNull final Instant occurredOn) {
        super(productDTO);
        this.occurredOn = occurredOn;
        this.createdOn = createdOn;
        this.productDTO = productDTO;
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }

    public Instant createdOn() {
        return createdOn;
    }

    public ProductDTO productDTO() {
        return productDTO;
    }
}
