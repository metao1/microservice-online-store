package com.metao.book.product.domain.model.event;

import com.metao.book.product.domain.model.valueobject.ImageUrl;
import com.metao.book.product.domain.model.valueobject.ProductDescription;
import com.metao.book.product.domain.model.valueobject.ProductTitle;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.product.ProductSku;
import com.metao.book.shared.domain.product.Quantity;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

/**
 * Domain event raised when a product is created
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class DomainProductCreatedEvent extends DomainEvent {

    private final ProductSku productSku;
    private final ProductTitle title;
    private final ImageUrl imageUrl;
    private final Quantity quantity;
    private final ProductTitle productTitle;
    private final ProductDescription description;
    private final Money price;

    public DomainProductCreatedEvent(
        @NonNull ProductSku productSku,
        @NonNull ProductTitle title,
        @NonNull ProductDescription description,
        @NonNull ImageUrl imageUrl,
        @NonNull Quantity quantity,
        @NonNull Money price,
        @NonNull Instant occurredOn
    ) {
        super(occurredOn);
        this.productSku = productSku;
        this.productTitle = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.title = title;
        this.price = price;
    }

    @NotNull
    @Override
    public String getEventType() {
        return "ProductCreated";
    }
}
