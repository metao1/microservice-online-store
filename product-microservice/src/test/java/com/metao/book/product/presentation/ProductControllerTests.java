package com.metao.book.product.presentation;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.metao.book.product.application.config.ProductMapper;
import com.metao.book.product.application.service.ProductService;
import com.metao.book.product.domain.ProductCategoryEntity;
import com.metao.book.product.domain.ProductRepository;
import com.metao.book.product.domain.category.Category;
import com.metao.book.product.util.BasePostgresIntegrationTest;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@ActiveProfiles({"test", "container"})
@WebMvcTest(controllers = ProductController.class)
@Import({ProductMapper.class, ProductService.class})
public class ProductControllerTests extends BasePostgresIntegrationTest {

    private static final String PRODUCT_URL = "/products/";
    private final String productId = UUID.randomUUID().toString();

    @MockBean
    ProductRepository productRepository;

    @Autowired
    MockMvc webTestClient;

    @Test
    public void loadOneProduct_isNotFound() throws Exception {
        webTestClient
            .perform(get(PRODUCT_URL + productId))
            .andExpect(status().isNotFound());
    }

    @Test
    public void loadOneProduct_isOk() throws Exception {
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
            .andExpect(jsonPath("$.imageUrl").value(pe.getImage().url()))
            .andExpect(jsonPath("$.currency").value("dlr"))
            .andExpect(jsonPath("$.price").value(BigDecimal.valueOf(12)));
    }

}
