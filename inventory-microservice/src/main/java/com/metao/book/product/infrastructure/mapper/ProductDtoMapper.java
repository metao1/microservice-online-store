package com.metao.book.product.infrastructure.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.product.application.dto.ProductEvent;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductDtoMapper implements DTOMapper<String, Optional<ProductEvent>> {

    private final ObjectMapper mapper;

    @Override
    public Optional<ProductEvent> convertToDto(String productString) {
        final ProductEvent productEvent;
        try {
            productEvent = mapper.readValue(productString, ProductEvent.class);
        } catch (JsonProcessingException ex) {
            log.error(ex.getMessage());
            return Optional.empty();
        }
        return Optional.of(productEvent);
    }

}
