package com.metao.book.product.infrastructure.util;

import com.metao.book.product.application.dto.ProductDTO;
import com.metao.book.product.domain.event.CreateProductEvent;
import lombok.experimental.UtilityClass;

import java.time.Instant;

@UtilityClass
public class EventUtil {

    public static CreateProductEvent createEvent(ProductDTO productDTO) {
        return new CreateProductEvent(productDTO.getAsin(), productDTO, Instant.now());
    }
}
