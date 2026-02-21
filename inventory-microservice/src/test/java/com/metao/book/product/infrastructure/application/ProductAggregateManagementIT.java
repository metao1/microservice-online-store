package com.metao.book.product.infrastructure.application;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;

import com.metao.shared.test.KafkaContainer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("test")
@TestPropertySource(properties = "kafka.enabled=true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Product Management Integration Tests")
public class ProductManagementIT extends KafkaContainer {

    @LocalServerPort
    private Integer port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    @DisplayName("should create product successfully")
    void shouldCreateProductSuccessfully() {
        // Given
        var requestBody = """
            {
                "sku": "SKU1234567",
                "title": "Test Product",
                "description": "Test Description",
                "image_url": "https://example.com/image.jpg",
                "price": 29.99,
                "currency": "EUR",
                "volume": 100,
                "categories": ["Books"]
            }
            """;

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/products")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .header("Location", endsWith("/products/SKU1234567"));
    }

    @Test
    @DisplayName("should replay create product request with same Idempotency-Key")
    void shouldReplayCreateProductRequestWithSameIdempotencyKey() {
        var requestBody = """
            {
                "sku": "SKU1234570",
                "title": "Idempotent Product",
                "description": "Idempotent Description",
                "image_url": "https://example.com/image.jpg",
                "price": 29.99,
                "currency": "EUR",
                "volume": 100,
                "categories": ["Books"]
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("Idempotency-Key", "req-123")
            .body(requestBody)
            .when()
            .post("/products")
            .then()
            .statusCode(HttpStatus.CREATED.value());

        given()
            .contentType(ContentType.JSON)
            .header("Idempotency-Key", "req-123")
            .body(requestBody)
            .when()
            .post("/products")
            .then()
            .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("should return 409 when same Idempotency-Key is reused for different SKU")
    void shouldReturn409WhenIdempotencyKeyReusedForDifferentSku() {
        var firstBody = """
            {
                "sku": "SKU1234571",
                "title": "First Product",
                "description": "First Description",
                "image_url": "https://example.com/image.jpg",
                "price": 29.99,
                "currency": "EUR",
                "volume": 100,
                "categories": ["Books"]
            }
            """;
        var secondBody = """
            {
                "sku": "SKU1234572",
                "title": "Second Product",
                "description": "Second Description",
                "image_url": "https://example.com/image.jpg",
                "price": 39.99,
                "currency": "EUR",
                "volume": 120,
                "categories": ["Books"]
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("Idempotency-Key", "req-456")
            .body(firstBody)
            .when()
            .post("/products")
            .then()
            .statusCode(HttpStatus.CREATED.value());

        given()
            .contentType(ContentType.JSON)
            .header("Idempotency-Key", "req-456")
            .body(secondBody)
            .when()
            .post("/products")
            .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    @DisplayName("should accept camelCase imageUrl when creating product")
    void shouldAcceptCamelCaseImageUrlWhenCreatingProduct() {
        // Given
        var requestBody = """
            {
                "sku": "SKU1234568",
                "title": "Test Product Camel",
                "description": "Test Description",
                "imageUrl": "https://example.com/image.jpg",
                "price": 29.99,
                "currency": "EUR",
                "volume": 100,
                "categories": ["Books"]
            }
            """;

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/products")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .header("Location", endsWith("/products/SKU1234568"));
    }

    @Test
    @DisplayName("should return 400 when creating product with invalid SKU format")
    void shouldReturn400WhenInvalidSkuFormat() {
        // Given - SKU must be exactly 10 characters
        var invalidRequestBody = """
            {
                "sku": "SHORT",
                "title": "Test Product",
                "description": "Test Description",
                "image_url": "https://example.com/image.jpg",
                "price": 29.99,
                "currency": "EUR",
                "volume": 100,
                "categories": ["Books"]
            }
            """;

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(invalidRequestBody)
            .when()
            .post("/products")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("should return 400 when creating product without required fields")
    void shouldReturn400WhenMissingRequiredFields() {
        // Given - Missing title
        var incompleteRequestBody = """
            {
                "sku": "SKU1234569",
                "description": "Test Description",
                "image_url": "https://example.com/image.jpg",
                "price": 29.99,
                "currency": "EUR",
                "volume": 100,
                "categories": ["Books"]
            }
            """;

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(incompleteRequestBody)
            .when()
            .post("/products")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("should return 400 when creating product without categories")
    void shouldReturn400WhenNoCategoriesProvided() {
        // Given - No categories
        var noCategoriesBody = """
            {
                "sku": "SKUWITHOUTC",
                "title": "Product Without Categories",
                "description": "Test Description",
                "image_url": "https://example.com/image.jpg",
                "price": 29.99,
                "currency": "EUR",
                "volume": 100,
                "categories": []
            }
            """;

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(noCategoriesBody)
            .when()
            .post("/products")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("should retrieve products by category")
    void shouldRetrieveProductsByCategory() {
        // Given - Create a product in a specific category
        String booksSku = "BOOK000001"; // Must be exactly 10 characters
        var booksBody = """
            {
                "sku": "%s",
                "title": "Clean Code",
                "description": "A Handbook of Agile Software Craftsmanship",
                "image_url": "https://example.com/cleancode.jpg",
                "price": 49.99,
                "currency": "EUR",
                "volume": 50,
                "categories": ["Books"]
            }
            """.formatted(booksSku);

        given()
            .contentType(ContentType.JSON)
            .body(booksBody)
            .when()
            .post("/products");

        // When & Then - Retrieve products by category and validate all fields in response
        given()
            .when()
            .get("/products/category/{category}", "Books")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("$", notNullValue())
            .body("$.size()", greaterThanOrEqualTo(1))
            .body("sku", hasItem(booksSku))
            .body("title", hasItem("Clean Code"))
            .body("description", hasItem("A Handbook of Agile Software Craftsmanship"))
            .body("imageUrl", hasItem("https://example.com/cleancode.jpg"))
            .body("price", hasItem(49.99f))
            .body("currency", hasItem("EUR"))
            .body("volume", hasItem(50))
            .body("categories", hasItem(containsInAnyOrder("Books")));
    }

    @Test
    @DisplayName("should search products by keyword with query parameters")
    void shouldSearchProductsByKeyword() {
        // Given
        String searchProductSku = "SRCH000001";
        var searchProductBody = """
            {
                "sku": "%s",
                "title": "Spring in Action",
                "description": "Learn Spring framework fundamentals",
                "image_url": "https://example.com/spring-book.jpg",
                "price": 59.99,
                "currency": "EUR",
                "volume": 30,
                "categories": ["Programming"]
            }
            """.formatted(searchProductSku);
        createProduct(searchProductBody);

        // When & Then
        given()
            .queryParam("keyword", "Spring")
            .queryParam("offset", 0)
            .queryParam("limit", 10)
            .when()
            .get("/products/search")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("sku", hasItem(searchProductSku))
            .body("title", hasItem("Spring in Action"))
            .body("description", hasItem("Learn Spring framework fundamentals"))
            .body("imageUrl", hasItem("https://example.com/spring-book.jpg"))
            .body("price", hasItem(59.99f))
            .body("currency", hasItem("EUR"))
            .body("volume", hasItem(30))
            .body("categories.flatten()", hasItem("Programming"));
    }

    @Test
    @DisplayName("should return empty list when search keyword has no matches")
    void shouldSearchReturnEmptyWhenNoMatches() {
        // When & Then
        given()
            .queryParam("keyword", "NonExistentKeywordXYZ123")
            .queryParam("offset", 0)
            .queryParam("limit", 10)
            .when()
            .get("/products/search")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("size()", equalTo(0));
    }

    @Test
    @DisplayName("should search products by keyword with different offset and limit")
    void shouldSearchProductsWithDifferentPagination() {
        // Given
        String productSku1 = "PROD000001";
        var productBody1 = """
            {
                "sku": "%s",
                "title": "Java Programming Basics",
                "description": "Learn Java fundamentals",
                "image_url": "https://example.com/java-book.jpg",
                "price": 45.99,
                "currency": "EUR",
                "volume": 40,
                "categories": ["Programming"]
            }
            """.formatted(productSku1);
        createProduct(productBody1);

        String productSku2 = "PROD000002";
        var productBody2 = """
            {
                "sku": "%s",
                "title": "Advanced Java Techniques",
                "description": "Master advanced Java concepts",
                "image_url": "https://example.com/advanced-java.jpg",
                "price": 65.99,
                "currency": "EUR",
                "volume": 25,
                "categories": ["Programming"]
            }
            """.formatted(productSku2);
        createProduct(productBody2);

        // When & Then
        given()
            .queryParam("keyword", "Java")
            .queryParam("offset", 0)
            .queryParam("limit", 5)
            .when()
            .get("/products/search")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("sku", hasItems(productSku1, productSku2))
            .body("title", hasItems("Java Programming Basics", "Advanced Java Techniques"));
    }

    @Test
    @DisplayName("should search products by partial keyword match")
    void shouldSearchProductsByPartialKeyword() {
        // Given
        String productSku = "TECH000001";
        var productBody = """
            {
                "sku": "%s",
                "title": "Python Programming Guide",
                "description": "Complete Python programming tutorial",
                "image_url": "https://example.com/python-book.jpg",
                "price": 54.99,
                "currency": "EUR",
                "volume": 35,
                "categories": ["Programming"]
            }
            """.formatted(productSku);
        createProduct(productBody);

        // When & Then
        given()
            .queryParam("keyword", "Python")
            .queryParam("offset", 0)
            .queryParam("limit", 10)
            .when()
            .get("/products/search")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("sku", hasItem(productSku))
            .body("title", hasItem("Python Programming Guide"));
    }

    @Test
    @DisplayName("should search products by description keyword")
    void shouldSearchProductsByDescriptionKeyword() {
        // Given
        String productSku = "DESC000001";
        var productBody = """
            {
                "sku": "%s",
                "title": "Web Development Book",
                "description": "Master modern web development with React and Node.js",
                "image_url": "https://example.com/web-dev.jpg",
                "price": 69.99,
                "currency": "EUR",
                "volume": 20,
                "categories": ["Programming"]
            }
            """.formatted(productSku);
        createProduct(productBody);

        // When & Then
        given()
            .queryParam("keyword", "React")
            .queryParam("offset", 0)
            .queryParam("limit", 10)
            .when()
            .get("/products/search")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("sku", hasItem(productSku))
            .body("description", hasItem("Master modern web development with React and Node.js"));
    }

    @Test
    @DisplayName("should search with case-insensitive keyword")
    void shouldSearchWithCaseInsensitiveKeyword() {
        // Given
        String productSku = "CASE000001";
        var productBody = """
            {
                "sku": "%s",
                "title": "Database Design Fundamentals",
                "description": "Learn SQL and database design",
                "image_url": "https://example.com/database.jpg",
                "price": 55.99,
                "currency": "EUR",
                "volume": 30,
                "categories": ["Programming"]
            }
            """.formatted(productSku);
        createProduct(productBody);

        // When & Then
        given()
            .queryParam("keyword", "database")
            .queryParam("offset", 0)
            .queryParam("limit", 10)
            .when()
            .get("/products/search")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("sku", hasItem(productSku))
            .body("title", hasItem("Database Design Fundamentals"));
    }

    @Test
    @DisplayName("should search products with default pagination")
    void shouldSearchProductsWithDefaultPagination() {
        // Given
        String productSku = "DEFAULT001";
        var productBody = """
            {
                "sku": "%s",
                "title": "Microservices Architecture",
                "description": "Design and build microservices",
                "image_url": "https://example.com/microservices.jpg",
                "price": 74.99,
                "currency": "EUR",
                "volume": 15,
                "categories": ["Programming"]
            }
            """.formatted(productSku);
        createProduct(productBody);

        // When & Then
        given()
            .queryParam("keyword", "Microservices")
            .when()
            .get("/products/search")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("sku", hasItem(productSku))
            .body("title", hasItem("Microservices Architecture"));
    }

    private void createProduct(String productBody) {
        given()
            .contentType(ContentType.JSON)
            .body(productBody)
            .when()
            .post("/products")
            .then()
            .statusCode(HttpStatus.CREATED.value());
    }
}
