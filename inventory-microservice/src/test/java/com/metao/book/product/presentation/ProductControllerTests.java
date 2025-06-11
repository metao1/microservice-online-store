package com.metao.book.product.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.metao.book.product.ProductCreatedEvent;
import com.metao.book.product.domain.Product;
import com.metao.book.product.domain.service.ProductService;
import com.metao.book.product.infrastructure.util.ProductConstant;
import com.metao.book.product.util.ProductEntityUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import lombok.SneakyThrows;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@TestPropertySource(properties = "kafka.enabled=false")
@WebMvcTest(controllers = ProductController.class)
class ProductControllerTests {

    private static final String PRODUCT_URL = "/products";

    @MockitoBean
    com.metao.book.product.application.service.kafkaProductProducer kafkaProductProducer;

    @MockitoBean
    ProductService productService;

    @Autowired
    MockMvc webTestClient;

    @Test
    void whenGetUnsavedProductThenReturnNotFound() throws Exception {
        var productId = UUID.randomUUID().toString();
        webTestClient.perform(get(PRODUCT_URL + productId)).andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void whenGetProductThenProductIsReturned() {
        var pe = ProductEntityUtils.createProductEntity();
        when(productService.getProductByAsin(pe.getAsin())).thenReturn(Optional.of(pe));

        webTestClient.perform(get("%s/%s".formatted(PRODUCT_URL, pe.getAsin())))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.asin").value(pe.getAsin())).
            andExpect(jsonPath("$.title").value(pe.getTitle()))
            .andExpect(jsonPath("$.description").value(pe.getDescription()))
            .andExpect(jsonPath("$.categories[0].category").value(ProductConstant.CATEGORY))
            .andExpect(jsonPath("$.imageUrl").value(pe.getImageUrl()))
            .andExpect(jsonPath("$.currency").value("EUR"))
            .andExpect(jsonPath("$.price").value(new BigDecimal("12.0")));
    }

    @Test
    @SneakyThrows
    void whenPostProductThenProductIsCreated() {
        var productDto = """
            {
                "asin": "1234567890",
                "description": "A sample description",
                "title": "Sample Product",
                "image_url": "https://example.com/image.jpg",
                "price": 99.99,
                "currency": "usd",
                "volume": 1.0,
                "categories": [{"category": "book"}],
                "also_bought": ["1234567891", "1234567892"]
             }
            """;

        doReturn(Boolean.TRUE).when(kafkaProductProducer)
            .sendEvent(any(ProductCreatedEvent.class));

        webTestClient.perform(post(PRODUCT_URL).contentType(MediaType.APPLICATION_JSON).content(productDto))
            .andExpect(status().isCreated()).andExpect(content().string("true"));

        verify(kafkaProductProducer).sendEvent(any(ProductCreatedEvent.class));
    }

    @Test
    void whenGetProductsThenProductsAreReturned() throws Exception {
        int limit = 10, offset = 0;
        var categories = List.of("book");
        String formattedCategories = String.join(",", categories);
        List<Product> pes = ProductEntityUtils.createMultipleProductEntity(limit);
        when(productService.getProductsByCategories(limit, offset, categories))
            .thenReturn(pes);

        // Load multiple products and verify responses
        // OPTION 1 - using for-loop and query multiple times
        for (Product pe : pes) {
            when(productService.getProductsByCategories(limit, offset, categories)).thenReturn(pes);
            webTestClient.perform(
                    get("%s/categories/%s?offset=%s&limit=%s".formatted(PRODUCT_URL, formattedCategories, offset, limit)))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$.[?(@.asin == '" + pe.getAsin() + "')]").exists())
                .andExpect(jsonPath("$.[?(@.title == '" + pe.getTitle() + "')]").exists())
                .andExpect(jsonPath("$.[?(@.description == '" + pe.getDescription() + "')]").exists())
                .andExpect(jsonPath("$.[?(@.imageUrl == '" + pe.getImageUrl() + "')]").exists())
                .andExpect(jsonPath("$.[?(@.currency == '" + pe.getPriceCurrency().toString() + "')]").exists())
                .andExpect(jsonPath("$.[?(@.price == " + pe.getPriceValue() + ")]").exists())
                .andExpect(jsonPath("$.[?(@.volume == " + pe.getVolume() + ")]").exists())
                .andExpect(jsonPath("$.[?(@.categories[0].category == 'book')]").exists());
        }

        when(productService.getProductsByCategories(limit, offset, categories)).thenReturn(pes);
        // OPTION 2 - using for-loop and query once and then verify responses using matcher -- preferable option
        webTestClient.perform(
                get("%s/categories/%s?offset=%s&limit=%s".formatted(PRODUCT_URL, formattedCategories, offset, limit)))
            .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(10))
            .andExpect(jsonPath("$[*].asin", extractFieldFromProducts(pes, Product::getAsin)))
            .andExpect(jsonPath("$[*].title", extractFieldFromProducts(pes, Product::getTitle)))
            .andExpect(jsonPath("$[*].description", extractFieldFromProducts(pes, Product::getDescription)))
            .andExpect(jsonPath("$[*].imageUrl", extractFieldFromProducts(pes, Product::getImageUrl)))
            .andExpect(
                jsonPath("$[*].currency", extractFieldFromProducts(pes, pe -> pe.getPriceCurrency().getCurrencyCode()))
            )
            .andExpect(jsonPath("$[*].volume", extractFieldFromProducts(pes, pe -> pe.getVolume().doubleValue())))
            .andExpect(jsonPath("$[*].categories[*].category", ProductConstant.CATEGORY).exists());
    }

    @Test
    @SneakyThrows
    void whenGetInvalidProductThenReturnNotFound() {
        var productId = UUID.randomUUID().toString();
        when(productService.getProductByAsin(productId)).thenReturn(Optional.empty());

        webTestClient.perform(get(PRODUCT_URL + productId)).andExpect(status().isNotFound());

        verifyNoMoreInteractions(productService);
    }

    private <T> Matcher<Iterable<?>> extractFieldFromProducts(
            List<Product> pes, Function<Product, T> extractor
    ) {
        return Matchers.containsInAnyOrder(pes.stream().map(extractor).toArray());
    }
}
