package com.metao.product.infrustructure.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductDtoMapperTest {

    @Test
    void givenString_convertToDto_thenIsOk() {
        var productDtoMapper = new ProductDtoMapper(new ObjectMapper());
        var productDto = productDtoMapper.convertToDto("{\"asin\": \"0439886341\", \"description\": \"Digital Organizer and Messenger\", \"title\": \"Digital Organizer and Messenger\", \"price\": 8.15, \"currency\":\"euro\", \"imageUrl\": \"http://localhost:8080/image.jpg\", \"categories\": [\"Electronics\", \"Computers & Accessories\", \"PDAs, Handhelds & Accessories\", \"PDAs & Handhelds\"]}");
        assertTrue(productDto.isPresent());
    }
}
