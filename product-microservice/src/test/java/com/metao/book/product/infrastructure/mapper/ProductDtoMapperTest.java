package com.metao.book.product.infrastructure.mapper;

import com.metao.book.product.ProductApplication;
import com.metao.book.product.infrastructure.factory.handler.FileHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest(classes = ProductApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
class ProductDtoMapperTest {
    @Autowired
    FileHandler fileHandler;

    @Autowired
    ProductDtoMapper dtoMapper;

    @Test
    void givenString_convertToDto_thenIsOk() throws IOException {
        var sb = new StringBuilder();
        try (var source = fileHandler.readFromFile("data/one_product.json")) {
            source.forEach(sb::append);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        assertTrue(Optional.of(sb.toString())
                .map(dtoMapper::convertToDto)
                .map(Optional::isPresent)
                .isPresent());
    }
}
