package com.metao.book.retails.infrustructure.util;

import java.time.Instant;

import com.metao.book.retails.application.dto.ProductDTO;
import com.metao.book.retails.domain.event.CreateProductEvent;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EventUtil {

        public static CreateProductEvent createEvent(ProductDTO productDTO) {
                return new CreateProductEvent(productDTO, Instant.now(), Instant.now());
        }
}
