package com.metao.book.retails.infrustructure.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.retails.ProductApplication;
import com.metao.book.retails.infrustructure.factory.handler.FileHandler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.ActiveProfiles;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@ActiveProfiles("test")
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
