package com.metao.book.product.presentation;

import com.metao.book.product.domain.Product;
import com.metao.book.product.domain.category.ProductCategory;
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

        productRepository.deleteAll();

        Money money10 = new Money(Currency.getInstance("USD"), BigDecimal.TEN);
        Money money20 = new Money(Currency.getInstance("USD"), BigDecimal.valueOf(20));

        product1 = new Product("ASIN001", "Laptop Pro", "High-end laptop", BigDecimal.valueOf(1.5), money10, "img1.jpg");
        
        categoryElectronics = new ProductCategory("Electronics");
        product1.addCategory(categoryElectronics); 
        productRepository.save(product1);

        product2 = new Product("ASIN002", "Gaming Mouse", "Optical gaming mouse", BigDecimal.valueOf(0.2), money20, "img2.jpg");
        productRepository.save(product2);
    }
    
    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
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
    void getProductsByCategories_singleCategory_whenProductsExist_returnsProductList() {
        ProductDTO[] products = given()
            .contentType(ContentType.JSON)
            .queryParam("offset", 0)
            .queryParam("limit", 10)
        .when()
            .get("/categories/{categoriesCsv}", categoryElectronics.getCategory()) 
        .then()
            .statusCode(HttpStatus.OK.value())
            .extract().as(ProductDTO[].class);

        assertThat(products).hasSize(1);
        assertThat(products[0].asin()).isEqualTo(product1.getAsin());
        assertThat(products[0].title()).isEqualTo(product1.getTitle());
    }

    @Test
    void getProductsByCategories_nonExistentCategory_returnsEmptyList() {
         ProductDTO[] products = given()
            .contentType(ContentType.JSON)
            .queryParam("offset", 0)
            .queryParam("limit", 10)
        .when()
            .get("/categories/{categoriesCsv}", "NonExistentCategory") 
        .then()
            .statusCode(HttpStatus.OK.value())
            .extract().as(ProductDTO[].class);

        assertThat(products).isEmpty();
    }

    @Test
    void getProductsByCategories_multipleCategories_findsProductInOne() {
        // product1 is in "Electronics" (categoryElectronics)
        String categoriesCsv = categoryElectronics.getCategory() + ",Books"; // "Electronics,Books"

        ProductDTO[] products = given()
            .contentType(ContentType.JSON)
            .queryParam("offset", 0)
            .queryParam("limit", 10)
        .when()
            .get("/categories/{categoriesCsv}", categoriesCsv)
        .then()
            .statusCode(HttpStatus.OK.value())
            .extract().as(ProductDTO[].class);

        assertThat(products).hasSize(1); 
        assertThat(products[0].asin()).isEqualTo(product1.getAsin());
    }

    @Test
    void getProductsByCategories_multipleCategoriesWithSpaces_parsesCorrectly() {
        String categoriesCsvWithSpaces = " " + categoryElectronics.getCategory() + " , Books "; 
        ProductDTO[] products = given()
            .contentType(ContentType.JSON)
            .queryParam("offset", 0)
            .queryParam("limit", 10)
        .when()
            .get("/categories/{categoriesCsv}", categoriesCsvWithSpaces)
        .then()
            .statusCode(HttpStatus.OK.value())
            .extract().as(ProductDTO[].class);

        assertThat(products).hasSize(1);
        assertThat(products[0].asin()).isEqualTo(product1.getAsin());
    }
    
    @Test
    void getProductsByCategories_emptyCategoryString_returnsEmptyList() {
        ProductDTO[] products = given()
            .contentType(ContentType.JSON)
            .queryParam("offset", 0)
            .queryParam("limit", 10)
        .when()
            .get("/categories/{categoriesCsv}", " ") // A space, which trims to empty
        .then()
            .statusCode(HttpStatus.OK.value())
            .extract().as(ProductDTO[].class);

        assertThat(products).isEmpty();
    }

    @Test
    void getProductsByCategories_onlyCommaSeparator_returnsEmptyList() {
        ProductDTO[] products = given()
            .contentType(ContentType.JSON)
            .queryParam("offset", 0)
            .queryParam("limit", 10)
        .when()
            .get("/categories/{categoriesCsv}", ",,,") 
        .then()
            .statusCode(HttpStatus.OK.value())
            .extract().as(ProductDTO[].class);

        assertThat(products).isEmpty();
    }
}
```
