package com.metao.product.infrustructure.util;

import java.time.Instant;

import com.metao.product.application.dto.ProductDTO;
import com.metao.product.domain.event.CreateProductEvent;

public class EventUtil {

        public static CreateProductEvent createEvent(ProductDTO productDTO) {
                return new CreateProductEvent(productDTO, Instant.now(), Instant.now());
        }
}
