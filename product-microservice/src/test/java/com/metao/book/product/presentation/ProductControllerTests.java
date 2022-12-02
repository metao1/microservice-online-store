package com.metao.book.product.presentation;
//
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.when;
//
//import com.metao.book.product.application.dto.ProductDTO;
//import com.metao.book.product.application.service.ProductService;
//import com.metao.book.product.domain.ProductCategoryEntity;
//import com.metao.book.product.domain.ProductCategoryRepository;
//import com.metao.book.product.domain.ProductEntity;
//import com.metao.book.product.domain.ProductId;
//import com.metao.book.product.domain.ProductRepository;
//import com.metao.book.product.domain.category.Category;
//import com.metao.book.product.domain.image.Image;
//import com.metao.book.product.infrastructure.mapper.ProductMapper;
//import com.metao.book.product.util.BasePostgresIntegrationTest;
//import com.metao.book.product.util.ProductTestUtils;
//import com.metao.book.shared.domain.financial.Currency;
//import com.metao.book.shared.domain.financial.Money;
//import java.math.BigDecimal;
//import java.util.Optional;
//import java.util.UUID;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
//import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.web.reactive.server.WebTestClient;
//import reactor.core.publisher.Mono;
//
//@ImportAutoConfiguration(classes = {
//    ProductMapper.class,
//    ProductService.class
//})
//@WebFluxTest(controllers = ProductController.class)
//@AutoConfigureTestDatabase(replace = Replace.NONE)
//public class ProductControllerTests extends BasePostgresIntegrationTest {
//
//    private static final String PRODUCT_URL = "/products/";
//    private final String productId = UUID.randomUUID().toString();
//    @MockBean
//    ProductRepository productRepository;
//    @MockBean
//    ProductCategoryRepository categoryRepository;
//    @Autowired
//    WebTestClient webTestClient;
//
//    @Test
//    public void loadOneProduct_isNotFound() {
//        webTestClient
//                .get()
//                .uri(PRODUCT_URL + productId)
//                .exchange()
//                .expectStatus()
//                .isNotFound();
//    }
//
//    @Test
//    public void loadOneProduct_isOk() {
//        var url = "https://example.com/image.jpg";
//        var description = "description";
//        var title = "title";
//        var price = BigDecimal.valueOf(12);
//        var id = "id";
//        var currency = Currency.DLR;
//        var category = new ProductCategoryEntity(new Category("book"));
//        var pe = new ProductEntity(id, title, description, new Money(currency, price), new Image(url));
//        pe.addCategory(category);
//        when(productRepository.findById(eq(new ProductId(id))))
//                .thenReturn(Optional.of(pe));
//        webTestClient
//            .get()
//            .uri(PRODUCT_URL + "details/" + pe.getIsin())
//                .exchange()
//                .expectStatus()
//                .isOk()
//                .expectBody()
//                .jsonPath("$.asin").exists()
//                .jsonPath("$.title").isEqualTo("title")
//                .jsonPath("$.description").isEqualTo("description")
//                .jsonPath("$.categories[0].category").isEqualTo("book")
//                .jsonPath("$.imageUrl").exists()
//                .jsonPath("$.currency").isEqualTo("dlr")
//                .jsonPath("$.price").isEqualTo(BigDecimal.valueOf(12));
//    }
//
//    @Test
//    public void testSaveProduct_isOk() {
//        var productDto = ProductTestUtils.createProductDTO();
//        webTestClient
//                .post()
//                .uri(PRODUCT_URL)
//                .body(Mono.just(productDto), ProductDTO.class)
//                .exchange()
//                .expectStatus()
//                .isOk()
//                .expectBody()
//                .isEmpty();
//    }
//}
