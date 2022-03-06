package com.metao.product.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import com.metao.ddd.finance.Currency;
import com.metao.ddd.finance.Money;
import com.metao.product.application.dto.ProductDTO;
import com.metao.product.application.service.ProductService;
import com.metao.product.domain.ProductCategoryEntity;
import com.metao.product.domain.ProductEntity;
import com.metao.product.domain.ProductId;
import com.metao.product.domain.ProductRepository;
import com.metao.product.domain.category.Category;
import com.metao.product.domain.image.Image;
import com.metao.product.infrustructure.mapper.ProductMapper;
import com.metao.product.util.ProductTestUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;

@Import({
                ProductMapper.class,
                ProductRepository.class,
                ProductService.class
})
@WebFluxTest(controllers = ProductController.class)
@ExtendWith(MockitoExtension.class)
public class ProductControllerTests {

        public static final String PRODUCT_URL = "/products/";

        @MockBean
        ProductRepository productRepository;

        @Autowired
        WebTestClient webTestClient;

        private final String productId = UUID.randomUUID().toString();

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
                var price = 12d;
                var currency = Currency.DLR;
                var category = new ProductCategoryEntity(new Category("book"));
                var pe = new ProductEntity(title, description, new Money(currency, price), new Image(url));
                pe.addCategory(category);
                when(productRepository.findProductEntityById(any(ProductId.class)))
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
                                .jsonPath("$.currency").isEqualTo("dollar")                                
                                .jsonPath("$.price").isEqualTo(1200d);
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
