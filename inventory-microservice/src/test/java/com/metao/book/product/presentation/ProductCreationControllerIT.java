package com.metao.book.product.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.product.application.dto.ProductDTO;
import com.metao.book.product.domain.ProductMapper;
import com.metao.book.product.event.ProductCreatedEvent;
import com.metao.book.product.infrastructure.factory.handler.ProductKafkaHandler;
import com.metao.book.product.infrastructure.mapper.ProductEventMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@TestPropertySource(
    properties = {
        "kafka.isEnabled=false"
    }
)
@AutoConfigureMockMvc
@WebMvcTest(controllers = ProductCreationController.class)
class ProductCreationControllerIT {

    private static final String PRODUCT_URL = "/products";

    @MockBean
    ProductKafkaHandler productKafkaHandler;

    @MockBean
    ProductMapper productMapper;

    @Autowired
    ObjectMapper objectMapper;

    @SpyBean
    ProductEventMapper ProductEventMapper;

    @Autowired
    MockMvc webTestClient;

    @Test
    public void testSaveProductIsOk() throws Exception {
        var productDto = """
            {
                "asin": "1234567890",
                "description": "A sample description",
                "title": "Sample Product",
                "image_url": "https://example.com/image.jpg",
                "price": 99.99,
                "currency": "usd",
                "volume": 1.0,
                "categories": []
             }
            """;

        webTestClient
            .perform(MockMvcRequestBuilders.post(PRODUCT_URL)
                .contentType(MediaType.APPLICATION_JSON).content(productDto))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("1234567890"));

        verify(productKafkaHandler).accept(any(ProductCreatedEvent.class));

        verify(ProductEventMapper).toEvent(any(ProductDTO.class));
    }
}