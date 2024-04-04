package com.metao.book.product.infrastructure.util;

import com.metao.book.product.application.dto.ProductEvent;
import com.metao.book.product.domain.event.ProductCreatedEvent;
import java.time.Instant;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EventUtil {

    public static ProductCreatedEvent createProductEvent(ProductEvent productEvent) {
        return new ProductCreatedEvent(productEvent.asin(), productEvent, Instant.now());
    }
}
