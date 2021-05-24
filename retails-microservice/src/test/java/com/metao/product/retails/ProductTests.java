package com.metao.product.retails;


import com.metao.product.retails.controller.ProductCatalogController;
import com.metao.product.retails.domain.ProductEntity;
import com.metao.product.retails.mapper.ProductCategoriesMapper;
import com.metao.product.retails.mapper.ProductMapper;
import com.metao.product.retails.model.ProductDTO;
import com.metao.product.retails.service.impl.ProductCategoriesServiceImplementation;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Import({ProductServiceImplementation.class, ProductCategoriesServiceImplementation.class})
@WebFluxTest(controllers = ProductCatalogController.class)
public class ProductTests extends BaseTest {

    public static final String PRODUCT_URL = "/products/";

    @MockBean
    ProductCategoriesMapper productCategoriesMapper;

    @MockBean
    ProductMapper productMapper;

    @Autowired
    WebTestClient webTestClient;

    private ProductEntity productEntity;

    private ProductDTO productDTO;


    @BeforeEach
    public void init(ApplicationContext context) {
        webTestClient = WebTestClient.bindToApplicationContext(context).build();

        productDTO = ProductDTO.builder()
                .asin(UUID.randomUUID().toString())
                .title(productEntity.getTitle())
                .categories(null)
                .price(productEntity.getPrice())
                .description(productEntity.getDescription())
                .imageUrl(productEntity.getImageUrl())
                .build();

        Mockito.when(productMapper.mapToEntity(productDTO)).thenReturn(productEntity);
        Mockito.when(productMapper.mapToDto(productEntity)).thenReturn(productDTO);
//        Mockito.when(productRepository.findProductEntityById(productId)).thenReturn(Optional.of(productEntity));
//        Mockito.when(productRepository.findProductEntityById(productId)).thenReturn(Optional.of(productEntity));

    }

    @Test
    public void testLoadOneProduct() {

        webTestClient.get().uri(PRODUCT_URL + PRODUCT_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductDTO.class)
                .value(val -> {
                    assertThat(val).isNotNull();
                    assertThat(val.getPrice()).isEqualTo(productEntity.getPrice());
                    assertThat(val.getTitle()).isEqualTo(productEntity.getTitle());
                    assertThat(val.getCategories().containsAll(productEntity.getCategories()));
                });
    }

    @Test
    public void testLoadAnonymousProduct_raisesError() {
        webTestClient.get().uri(PRODUCT_URL + PRODUCT_ID)
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
