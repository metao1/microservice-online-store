package com.metao.book.retails.infrastructure.util;

import com.metao.book.retails.application.dto.ProductDTO;
import com.metao.book.retails.domain.event.CreateProductEvent;
import lombok.experimental.UtilityClass;

import java.time.Instant;

@UtilityClass
public class EventUtil {

    public static CreateProductEvent createEvent(ProductDTO productDTO) {
        return new CreateProductEvent(productDTO.getAsin(), productDTO, Instant.now());
    }
}
