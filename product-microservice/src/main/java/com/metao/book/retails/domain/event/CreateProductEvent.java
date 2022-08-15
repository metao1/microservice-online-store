package com.metao.book.retails.domain.event;

import com.metao.book.retails.application.dto.ProductDTO;
import com.metao.book.shared.domain.base.DomainEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Getter
@RequiredArgsConstructor
public class CreateProductEvent implements DomainEvent {

    private final String id;
    private final ProductDTO productDTO;
    private final Instant occurredOn;
}
