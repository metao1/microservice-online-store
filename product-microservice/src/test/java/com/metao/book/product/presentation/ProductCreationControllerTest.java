package com.metao.book.product.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.product.infrastructure.factory.handler.ProductKafkaHandler;
import com.metao.book.product.util.ProductTestUtils;
import com.metao.book.shared.test.StreamBuilderTestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@ActiveProfiles({"test", "container"})
@WebMvcTest(controllers = ProductCreationController.class)
class ProductCreationControllerTest {

    private static final String PRODUCT_URL = "/products";

    @MockBean
    ProductKafkaHandler productKafkaHandler;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc webTestClient;

    @Test
    public void testSaveProduct_isOk() throws Exception {
        var productDto = ProductTestUtils.createProductDTO();
        webTestClient
            .perform(post(PRODUCT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(StreamBuilderTestUtils.convertObjectToJsonBytes(objectMapper, productDto))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$").doesNotExist());
    }
}