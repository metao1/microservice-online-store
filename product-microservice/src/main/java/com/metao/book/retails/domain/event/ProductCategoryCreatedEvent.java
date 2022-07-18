package com.metao.book.retails.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.metao.book.retails.domain.category.CategoryId;
import com.metao.book.shared.domain.base.DomainEvent;

import javax.validation.constraints.NotNull;
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
    @NotNull
    public Instant occurredOn() {
        return occurredOn;
    }

    public CategoryId getCategoryId() {
        return categoryId;
    }
}
