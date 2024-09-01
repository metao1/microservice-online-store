package com.metao.book.product.presentation;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.metao.book.product.application.service.ProductService;
import com.metao.book.product.domain.ProductCategoryEntity;
import com.metao.book.product.domain.ProductMapper;
import com.metao.book.product.domain.ProductRepository;
import com.metao.book.product.domain.category.Category;
import com.metao.book.product.util.ProductTestUtils;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@WebMvcTest(controllers = ProductController.class)
@Import({ProductMapper.class, ProductService.class})
class ProductControllerTests {

    private static final String PRODUCT_URL = "/products/";
    private final String productId = UUID.randomUUID().toString();

    @MockBean
    ProductRepository productRepository;

    @Autowired
    MockMvc webTestClient;

    @Test
    void loadOneProductIsNotFound() throws Exception {
        webTestClient
            .perform(get(PRODUCT_URL + productId))
            .andExpect(status().isNotFound());
    }

    @Test
    void loadOneProductIsOk() throws Exception {
        var category = new ProductCategoryEntity(new Category("book"));
        var pe = ProductTestUtils.createProductEntity();
        pe.addCategory(category);
        when(productRepository.findByAsin(eq(pe.getAsin())))
            .thenReturn(Optional.of(pe));
        webTestClient
            .perform(get(PRODUCT_URL + "details/" + pe.getAsin()))
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

}
