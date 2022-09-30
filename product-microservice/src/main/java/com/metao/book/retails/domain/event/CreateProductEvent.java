package com.metao.book.retails.domain.event;

import com.metao.book.retails.application.dto.ProductDTO;
import com.metao.book.shared.domain.base.DomainEvent;
import lombok.Value;

import java.time.Instant;

@Value
public class CreateProductEvent implements DomainEvent {

    String id;
    ProductDTO productDTO;
    Instant occurredOn;
}
