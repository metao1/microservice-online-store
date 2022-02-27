package com.metao.product;

import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import com.metao.ddd.finance.Currency;
import com.metao.ddd.finance.Money;
import com.metao.product.application.dto.ProductDTO;
import com.metao.product.application.persistence.ProductRepository;
import com.metao.product.application.service.ProductService;
import com.metao.product.domain.ProductEntity;
import com.metao.product.domain.ProductId;
import com.metao.product.domain.category.CategoryEntity;
import com.metao.product.domain.image.Image;
import com.metao.product.infrustructure.mapper.ProductMapperInterface;
import com.metao.product.presentation.ProductCatalogController;

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
        ProductMapperInterface.class,
        ProductMapperInterface.class,
        ProductService.class,
        ProductRepository.class
})
@WebFluxTest(controllers = ProductCatalogController.class)
@ExtendWith(MockitoExtension.class)
public class ProductTests extends BaseTest {

    public static final String PRODUCT_URL = "/products/";

    @MockBean
    ProductMapperInterface productMapper;

    @MockBean
    ProductRepository productRepository;

    @Autowired
    WebTestClient webTestClient;

    private String productId = UUID.randomUUID().toString();

    private ProductDTO productDTO;

    //
    // @BeforeEach
    // public void init(ApplicationContext context) {
    // webTestClient = WebTestClient.bindToApplicationContext(context).build();
    // productEntity = ProductEntity.builder()
    // .title("brand")
    // .categories(Collections.singleton(ProductCategoryEntity.builder()
    // .id(UUID.randomUUID().toString())
    // .categories("Book").build()))
    // .description("clothes")
    // .id(productId)
    // .createdAt(NOW())
    // .modifiedAt(NOW())
    // .createdBy(USER_ID)
    // .modifiedBy(USER_ID)
    // .imageUrl("image-url")
    // .price(1200d)
    // .build();
    // productDTO = ProductDTO.builder()
    // .asin(UUID.randomUUID().toString())
    // .title(productEntity.getTitle())
    // .categories(null)
    // .price(productEntity.getPrice())
    // .description(productEntity.getDescription())
    // .imageUrl(productEntity.getImage())
    // .build();
    //
    // Mockito.when(productMapper.mapToEntity(productDTO)).thenReturn(productEntity);
    // Mockito.when(productMapper.mapToDto(productEntity)).thenReturn(productDTO);
    //// Mockito.when(productRepository.findProductEntityById(productId)).thenReturn(Optional.of(productEntity));
    //// Mockito.when(productRepository.findProductEntityById(productId)).thenReturn(Optional.of(productEntity));
    //
    // }

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
        var url = "http://localhost:8080/image.jpg";
        var description = "description";
        var title = "title";
        var price = 12d;
        var currency = Currency.DLR;
        var category = new CategoryEntity("book");
        var pe = new ProductEntity(title, description, new Money(currency, price), new Image(url));
        pe.addCategory(category);
        when(productRepository.findProductEntityById(new ProductId(productId)))
                .thenReturn(Optional.of(pe));

        webTestClient
                .get()
                .uri(PRODUCT_URL + "details/"+ productId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.[0].id");
        // .value(val -> {
        // assertThat(val).isNotNull();
        // assertThat(val.getPrice()).isEqualTo(productEntity.getPriceValue());
        // assertThat(val.getTitle()).isEqualTo(productEntity.getTitle());
        // // assertThat(val.getCategories().containsAll(productEntity.ge()));
        // });
    }

    @Test
    public void testLoadAnonymousProduct_raisesError() {
        webTestClient.get().uri(PRODUCT_URL + productId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void testSaveProduct_isOk() {
        webTestClient.post()
                .uri(PRODUCT_URL)
                .body(Mono.just(productDTO), ProductDTO.class)
                .exchange()
                .expectBody().isEmpty();
    }
}
