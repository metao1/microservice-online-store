package com.metao.book.product.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.metao.book.product.application.dto.ProductDTO;
import com.metao.book.product.application.service.ProductService;
import com.metao.book.product.domain.ProductCategoryEntity;
import com.metao.book.product.domain.ProductMapper;
import com.metao.book.product.domain.ProductRepository;
import com.metao.book.product.domain.category.Category;
import com.metao.book.product.event.ProductCreatedEvent;
import com.metao.book.product.infrastructure.factory.handler.ProductKafkaHandler;
import com.metao.book.product.util.ProductTestUtils;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@TestPropertySource(
    properties = {
        "kafka.isEnabled=false"
    }
)
@AutoConfigureMockMvc
@WebMvcTest(controllers = ProductController.class)
@Import({ProductMapper.class, ProductService.class, ProductKafkaHandler.class})
class ProductControllerTests {

    private static final String PRODUCT_URL = "/products";

    @MockBean
    ProductRepository productRepository;

    @MockBean
    ProductKafkaHandler productKafkaHandler;

    @SpyBean
    ProductMapper productMapper;

    @Autowired
    MockMvc webTestClient;

    @Test
    void loadOneProductIsNotFound() throws Exception {
        var productId = UUID.randomUUID().toString();
        webTestClient
            .perform(get(PRODUCT_URL + productId))
            .andExpect(status().isNotFound());
    }

    @Test
    void loadOneProductIsOk() throws Exception {
        var category = new ProductCategoryEntity(new Category("book"));
        var pe = ProductTestUtils.createProductEntity();
        pe.addCategory(category);

        when(productRepository.findByAsin(pe.getAsin()))
            .thenReturn(Optional.of(pe));

        webTestClient
            .perform(get(String.format("%s/%s", PRODUCT_URL, pe.getAsin())))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.asin").value(pe.getAsin()))
            .andExpect(jsonPath("$.title").value(pe.getTitle()))
            .andExpect(jsonPath("$.description").value(pe.getDescription()))
            .andExpect(jsonPath("$.categories[0].category").value("book"))
            .andExpect(jsonPath("$.image_url").value(pe.getImage().url()))
            .andExpect(jsonPath("$.currency").value("EUR"))
            .andExpect(jsonPath("$.price").value(BigDecimal.valueOf(12)));
    }

    @Test
    void testSaveProductIsOk() throws Exception {
        var productDto = """
            {
                "asin": "1234567890",
                "description": "A sample description",
                "title": "Sample Product",
                "image_url": "https://example.com/image.jpg",
                "price": 99.99,
                "currency": "usd",
                "volume": 1.0,
                "categories": ["book"]
             }
            """;

        webTestClient
            .perform(post(PRODUCT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(productDto)
            )
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("1234567890"));

        verify(productKafkaHandler).accept(any(ProductCreatedEvent.class));

        verify(productMapper).toEvent(any(ProductDTO.class));
    }
}
