package com.metao.book.product.domain.event;

import com.metao.book.product.application.dto.ProductEvent;
import com.metao.book.shared.domain.base.DomainEvent;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import lombok.Builder;

@Builder
public record ProductCreatedEvent(@NotNull String id,
                                  @NotNull ProductEvent productEvent,
                                  @NotNull Instant occurredOn) implements Serializable, DomainEvent {
}
