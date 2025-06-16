package com.metao.book.product.domain.model.event;

import com.metao.book.product.domain.model.valueobject.ProductId;
import com.metao.book.product.domain.model.valueobject.ProductTitle;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.financial.Money;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

/**
 * Domain event raised when a product is created
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class ProductCreatedEvent extends DomainEvent {

    private final ProductId productId;
    private final ProductTitle title;
    private final Money price;

    public ProductCreatedEvent(
        @NonNull ProductId productId,
        @NonNull ProductTitle title,
        @NonNull Money price
    ) {
        super(LocalDateTime.now());
        this.productId = productId;
        this.title = title;
        this.price = price;
    }

    @Override
    public String getEventType() {
        return "ProductCreated";
    }
}
