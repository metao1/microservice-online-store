package com.metao.book.product.domain.model.event;

import com.metao.book.product.domain.model.valueobject.ProductSku;
import com.metao.book.product.domain.model.valueobject.ProductTitle;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.financial.Money;
import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * Domain event raised when a product is created
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class ProductCreatedEvent extends DomainEvent {

    private final ProductSku productSku;
    private final ProductTitle title;
    private final Money price;

    public ProductCreatedEvent(
        @NonNull ProductSku productSku,
        @NonNull ProductTitle title,
            @NonNull Money price,
            @NonNull Instant occurredOn
    ) {
        super(occurredOn);
        this.productSku = productSku;
        this.title = title;
        this.price = price;
    }

    @NotNull
    @Override
    public String getEventType() {
        return "ProductCreated";
    }
}
