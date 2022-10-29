package com.metao.book.product.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.metao.book.product.domain.category.CategoryId;
import com.metao.book.shared.domain.base.DomainEvent;
import lombok.Value;

import java.time.Instant;

@Value
public class ProductCategoryCreatedEvent implements DomainEvent {

    String id;
    @JsonProperty("occurred_on")
    Instant occurredOn;
    @JsonProperty("category_id")
    CategoryId categoryId;

}
