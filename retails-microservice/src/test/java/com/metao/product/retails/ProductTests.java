package com.metao.product.retails;

import com.metao.product.retails.controller.ProductCatalogController;
import com.metao.product.retails.domain.ProductEntity;
import com.metao.product.retails.mapper.ProductMapper;
import com.metao.product.retails.model.ProductDTO;
import com.metao.product.retails.persistence.ProductRepository;
import com.metao.product.retails.service.impl.ProductServiceImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Import(ProductServiceImplementation.class)
@WebFluxTest(controllers = ProductCatalogController.class)
public class ProductTests extends BaseTest {

    public static final String PRODUCT_URL = "/products/";
    @MockBean
    ProductRepository productRepository;

    @MockBean
    ProductMapper productMapper;

    @Autowired
    WebTestClient webTestClient;

    private String productId = UUID.randomUUID().toString();
    private ProductEntity productEntity = ProductEntity.builder().brand("brand")
            .categories(Collections.singleton("Clothes"))
            .description("clothes")
            .id(productId)
            .createdAt(NOW())
            .modifiedAt(NOW())
            .createdBy(USER_ID)
            .modifiedBy(USER_ID)
            .imageUrl("image-url")
            .price(1200d)
            .build();

    private ProductDTO productDTO = ProductDTO.builder()
            .asin(UUID.randomUUID().toString())
            .brand(productEntity.getBrand())
            .categories(productEntity.getCategories())
            .createdAt(productEntity.getCreatedAt())
            .createdBy(productEntity.getCreatedBy())
            .modifiedAt(productEntity.getModifiedAt())
            .price(productEntity.getPrice())
            .modifiedBy(productEntity.getModifiedBy())
            .description(productEntity.getDescription())
            .imageUrl(productEntity.getImageUrl())
            .build();


    @BeforeEach
    public void init(ApplicationContext context) {
        webTestClient = WebTestClient.bindToApplicationContext(context).build();
        Mockito.when(productMapper.toEntity(productDTO)).thenReturn(productEntity);
        Mockito.when(productMapper.toDto(productEntity)).thenReturn(productDTO);
    }

    @Test
    public void testLoadOneProduct() {
        Mockito.when(productRepository.findProductEntityById(productId)).thenReturn(Optional.of(productEntity));
        webTestClient.get().uri(PRODUCT_URL + productId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductDTO.class)
                .value(val -> {
                    assertThat(val).isNotNull();
                    assertThat(val.getPrice()).isEqualTo(productEntity.getPrice());
                    assertThat(val.getBrand()).isEqualTo(productEntity.getBrand());
                    assertThat(val.getCategories().containsAll(productEntity.getCategories()));
                });
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
