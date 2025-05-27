package com.metao.book.product.presentation;

import com.metao.book.product.domain.Product;
import com.metao.book.product.domain.category.ProductCategory; // Added for category test
import com.metao.book.product.domain.dto.ProductDTO;
import com.metao.book.product.infrastructure.repository.ProductRepository;
import com.metao.book.shared.domain.financial.Money;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Currency;
// import java.util.List; // Not directly used, ProductDTO[] is used

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductControllerIT {

    @LocalServerPort
    private Integer port;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("bookstore")
            .withUsername("bookstore")
            .withPassword("bookstore");

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }

    @Autowired
    private ProductRepository productRepository;

    private Product product1, product2;
    private ProductCategory categoryElectronics;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/products"; 

        productRepository.deleteAll(); // Clean up before each test

        Money money10 = new Money(Currency.getInstance("USD"), BigDecimal.TEN);
        Money money20 = new Money(Currency.getInstance("USD"), BigDecimal.valueOf(20));

        product1 = new Product("ASIN001", "Laptop Pro", "High-end laptop", BigDecimal.valueOf(1.5), money10, "img1.jpg");
        
        // Setup category
        categoryElectronics = new ProductCategory("Electronics");
        // The Product entity has CascadeType.PERSIST and MERGE for categories.
        // This means saving the Product should also save the new ProductCategory if it's not already managed.
        // However, ProductCategory itself has an ID and might need to be managed/found first if it could already exist.
        // For simplicity in this test, we assume new categories are created and cascaded.
        // If ProductCategory had its own repository, we might save it there first.
        // For this test, we'll add it to the product and save the product.
        product1.addCategory(categoryElectronics); 
        productRepository.save(product1);

        product2 = new Product("ASIN002", "Gaming Mouse", "Optical gaming mouse", BigDecimal.valueOf(0.2), money20, "img2.jpg");
        productRepository.save(product2);
    }
    
    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
        // If ProductCategoryRepository was used, cleanup categories too.
        // Since we rely on cascade from Product for categories, deleteAll on products should be enough if orphanRemoval is not configured.
        // For safety, if categories were independently saved, they'd need explicit deletion.
        // Given the ManyToMany, deleting products won't delete categories unless they are orphaned and orphanRemoval=true on the other side (not typical for ManyToMany).
        // However, for this test scope, productRepository.deleteAll() is the primary cleanup.
    }

    @Test
    void getProductByAsin_whenExists_returnsProduct() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/{asin}", product1.getAsin())
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("asin", equalTo(product1.getAsin()))
            .body("title", equalTo(product1.getTitle()));
    }

    @Test
    void getProductByAsin_whenNotExists_returnsNotFound() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/{asin}", "NONEXISTENTASIN")
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }
    
    @Test
    void searchProductsByKeyword_findsMatchingProducts() {
        ProductDTO[] products = given()
            .contentType(ContentType.JSON)
            .queryParam("keyword", "Laptop")
        .when()
            .get("/search")
        .then()
            .statusCode(HttpStatus.OK.value())
            .extract().as(ProductDTO[].class);

        assertThat(products).hasSize(1);
        assertThat(products[0].asin()).isEqualTo(product1.getAsin());
    }

    @Test
    void searchProductsByKeyword_whenNoMatch_returnsEmptyList() {
        ProductDTO[] products = given()
            .contentType(ContentType.JSON)
            .queryParam("keyword", "NonExistentKeyword")
        .when()
            .get("/search")
        .then()
            .statusCode(HttpStatus.OK.value())
            .extract().as(ProductDTO[].class);

        assertThat(products).isEmpty();
    }
    
    @Test
    void getProductsByCategory_whenProductsExist_returnsProductList() {
        ProductDTO[] products = given()
            .contentType(ContentType.JSON)
            .queryParam("offset", 0) // Add required params for pagination
            .queryParam("limit", 10)
        .when()
            .get("/category/{name}", categoryElectronics.getCategory())
        .then()
            .statusCode(HttpStatus.OK.value())
            .extract().as(ProductDTO[].class);

        assertThat(products).hasSize(1);
        assertThat(products[0].asin()).isEqualTo(product1.getAsin());
        assertThat(products[0].title()).isEqualTo(product1.getTitle());
    }

    @Test
    void getProductsByCategory_whenCategoryDoesNotExistOrNoProducts_returnsEmptyList() {
         ProductDTO[] products = given()
            .contentType(ContentType.JSON)
            .queryParam("offset", 0)
            .queryParam("limit", 10)
        .when()
            .get("/category/{name}", "NonExistentCategory")
        .then()
            .statusCode(HttpStatus.OK.value()) // Controller returns List<ProductDTO>, so OK with empty list
            .extract().as(ProductDTO[].class);

        assertThat(products).isEmpty();
    }
}
```
