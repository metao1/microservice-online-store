package com.metao.book.product.domain.model.event;

import com.metao.book.product.domain.model.valueobject.ProductSku;
import com.metao.book.product.domain.model.valueobject.ProductTitle;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.financial.Money;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * Domain event raised when a product is updated
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class ProductUpdatedEvent extends DomainEvent {

    private final ProductSku productSku;
    private final ProductTitle title;
    private final Money oldPrice;
    private final Money newPrice;

    public ProductUpdatedEvent(
        @NonNull ProductSku productSku,
        @NonNull ProductTitle title,
        @NonNull Money oldPrice,
        @NonNull Money newPrice
    ) {
        super(LocalDateTime.now());
        this.productSku = productSku;
        this.title = title;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
    }

    @NotNull
    @Override
    public String getEventType() {
        return "ProductUpdated";
    }
}
