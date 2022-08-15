package com.metao.book.retails.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.metao.book.retails.domain.category.CategoryId;
import com.metao.book.shared.domain.base.DomainEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Getter
@RequiredArgsConstructor
public class ProductCategoryCreatedEvent implements DomainEvent {

    private final String id;
    @JsonProperty("occurred_on")
    private final Instant occurredOn;
    @JsonProperty("category_id")
    private final CategoryId categoryId;

}
