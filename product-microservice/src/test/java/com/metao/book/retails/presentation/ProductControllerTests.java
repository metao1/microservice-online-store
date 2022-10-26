package com.metao.book.retails.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.metao.book.retails.application.dto.ProductDTO;
import com.metao.book.retails.application.service.ProductService;
import com.metao.book.retails.domain.ProductCategoryEntity;
import com.metao.book.retails.domain.ProductCategoryRepository;
import com.metao.book.retails.domain.ProductEntity;
import com.metao.book.retails.domain.ProductId;
import com.metao.book.retails.domain.ProductRepository;
import com.metao.book.retails.domain.category.Category;
import com.metao.book.retails.domain.image.Image;
import com.metao.book.retails.infrastructure.mapper.ProductMapper;
import com.metao.book.retails.util.ProductTestUtils;
import com.metao.book.shared.domain.financial.Currency;
import com.metao.book.shared.domain.financial.Money;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@Import({
        ProductMapper.class,
        ProductService.class
})
@WebFluxTest(controllers = ProductController.class)
public class ProductControllerTests {

    private static final String PRODUCT_URL = "/products/";
    private final String productId = UUID.randomUUID().toString();
    @MockBean
    ProductRepository productRepository;
    @MockBean
    ProductCategoryRepository categoryRepository;
    @Autowired
    WebTestClient webTestClient;

    @Test
    public void loadOneProduct_isNotFound() {
        webTestClient
                .get()
                .uri(PRODUCT_URL + productId)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    public void loadOneProduct_isOk() {
        var url = "https://example.com/image.jpg";
        var description = "description";
        var title = "title";
        var price = BigDecimal.valueOf(12);
        var id = "id";
        var currency = Currency.DLR;
        var category = new ProductCategoryEntity(new Category("book"));
        var pe = new ProductEntity(id, title, description, new Money(currency, price), new Image(url));
        pe.addCategory(category);
        when(productRepository.findById(any(ProductId.class)))
                .thenReturn(Optional.of(pe));
        webTestClient
                .get()
                .uri(PRODUCT_URL + "details/" + pe.id().toUUID())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.asin").exists()
                .jsonPath("$.title").isEqualTo("title")
                .jsonPath("$.description").isEqualTo("description")
                .jsonPath("$.categories[0].category").isEqualTo("book")
                .jsonPath("$.imageUrl").exists()
                .jsonPath("$.currency").isEqualTo("dlr")
                .jsonPath("$.price").isEqualTo(12d);
    }

    @Test
    public void testSaveProduct_isOk() {
        var productDto = ProductTestUtils.createProductDTO();
        webTestClient
                .post()
                .uri(PRODUCT_URL)
                .body(Mono.just(productDto), ProductDTO.class)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .isEmpty();
    }
}
