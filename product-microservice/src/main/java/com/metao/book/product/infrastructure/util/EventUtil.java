package com.metao.book.product.infrastructure.util;

import com.metao.book.product.application.dto.ProductDTO;
import com.metao.book.product.domain.event.CreateProductEvent;
import java.time.Instant;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EventUtil {

    public static CreateProductEvent createProductEvent(ProductDTO productDTO) {
        return new CreateProductEvent(productDTO.asin(), productDTO, Instant.now());
    }
}
