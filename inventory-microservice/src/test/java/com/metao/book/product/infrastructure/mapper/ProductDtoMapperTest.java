package com.metao.book.product.infrastructure.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.shared.application.service.FileHandler;
import java.io.IOException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class ProductDtoMapperTest {

    ProductDtoMapper dtoMapper = new ProductDtoMapper(new ObjectMapper());

    @Test
    void givenString_toDto_thenIsOk() throws IOException {
        var sb = new StringBuilder();
        try (var source = FileHandler.readResourceInPath(getClass(), "data/one_product.json")) {
            source.forEach(sb::append);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        assertTrue(Optional.of(sb.toString())
            .map(dtoMapper::toDto)
            .map(Optional::isPresent)
            .isPresent());
    }
}
