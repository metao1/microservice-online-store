package com.metao.product.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.metao.ddd.shared.domain.base.DomainEvent;
import com.metao.product.domain.category.CategoryId;

import java.time.Instant;

public class ProductCategoryCreatedEvent implements DomainEvent {

    private final Instant occurredOn;
    private final CategoryId categoryId;

    @JsonCreator
    ProductCategoryCreatedEvent(@JsonProperty("category_id") CategoryId categoryId,
            @JsonProperty("occurred_on") Instant occurredOn) {
        this.occurredOn = occurredOn;
        this.categoryId = categoryId;
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }

    public CategoryId getCategoryId() {
        return categoryId;
    }
}
