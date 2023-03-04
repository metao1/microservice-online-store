package com.metao.book.product.domain.event;

import com.metao.book.product.application.dto.ProductDTO;
import com.metao.book.shared.domain.base.DomainEvent;
import java.time.Instant;

public record CreateProductEvent(String id, ProductDTO productDto, Instant occurredOn) implements DomainEvent {

}
