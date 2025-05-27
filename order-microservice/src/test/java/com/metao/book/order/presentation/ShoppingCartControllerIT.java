package com.metao.book.order.presentation;

import com.metao.book.order.application.cart.ShoppingCart;
import com.metao.book.order.application.cart.ShoppingCartRepository;
import com.metao.book.order.application.cart.UpdateCartItemQtyDTO;
import com.metao.book.order.presentation.dto.AddItemRequestDTO;
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
import static org.hamcrest.Matchers.*;
import static org.assertj.core.api.Assertions.assertThat;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShoppingCartControllerIT {

    @LocalServerPort
    private Integer port;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("bookstore-order") // Database for order-microservice
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
    private ShoppingCartRepository shoppingCartRepository;

    private String userId1;
    private String asin1;
    private String asin2;
    private Currency currency;
    private ShoppingCart cartItem1User1;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/cart"; // Base path for ShoppingCartController

        shoppingCartRepository.deleteAll(); // Clean up before each test

        userId1 = "user123";
        asin1 = "ASIN001";
        asin2 = "ASIN002";
        currency = Currency.getInstance("USD");

        // Initial item for user1
        // Constructor: public ShoppingCart(String userId, String asin, BigDecimal buyPrice, BigDecimal sellPrice, BigDecimal quantity, Currency currency)
        cartItem1User1 = new ShoppingCart(userId1, asin1, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ONE, currency);
        // Need to set createdOn and updatedOn as the entity might expect them (e.g. non-null db constraints if any, or for DTO mapping)
        // The constructor ShoppingCart(...) sets createdOn. Let's assume updatedOn is also set or can be null initially.
        // For safety, let's set it here if the entity expects it. The entity has @NoArgsConstructor, so fields can be null.
        // The constructor used here sets createdOn. updatedOn will be set upon actual update.
        shoppingCartRepository.save(cartItem1User1);
    }

    @AfterEach
    void tearDown() {
        shoppingCartRepository.deleteAll();
    }

    @Test
    void getCartByUserId_whenCartExists_returnsCart() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/{userId}", userId1)
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("user_id", equalTo(userId1)) // Matches ShoppingCartDto's @JsonProperty
            .body("shopping_cart_items", hasSize(1))
            .body("shopping_cart_items[0].asin", equalTo(asin1))
            // Using closeTo for BigDecimal comparisons with Hamcrest for robustness
            .body("shopping_cart_items[0].quantity", closeTo(1.0, 0.001)) 
            .body("shopping_cart_items[0].price", closeTo(10.0, 0.001));
    }
    
    @Test
    void getCartByUserId_whenCartDoesNotExist_returnsEmptyCart() {
        // Assuming service returns a ShoppingCartDto with an empty item list
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/{userId}", "nonexistentuser")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("user_id", equalTo("nonexistentuser")) // Service populates userId in DTO
            .body("shopping_cart_items", empty());
    }

    @Test
    void addItemToCart_newItem_returnsCreatedItem() {
        AddItemRequestDTO newItemDto = new AddItemRequestDTO(BigDecimal.valueOf(2), BigDecimal.valueOf(20.00), currency);

        ShoppingCart returnedCart = given()
            .contentType(ContentType.JSON)
            .body(newItemDto)
        .when()
            .post("/{userId}/{asin}", userId1, asin2) // New ASIN for user1
        .then()
            .statusCode(HttpStatus.OK.value()) // Controller returns 200 OK with the ShoppingCart entity
            .extract().as(ShoppingCart.class);

        assertThat(returnedCart.getUserId()).isEqualTo(userId1);
        assertThat(returnedCart.getAsin()).isEqualTo(asin2);
        assertThat(returnedCart.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(2));
        assertThat(returnedCart.getSellPrice()).isEqualByComparingTo(BigDecimal.valueOf(20.00));
        
        // Verify in DB
        ShoppingCart dbItem = shoppingCartRepository.findByUserIdAndAsin(userId1, asin2).orElse(null);
        assertThat(dbItem).isNotNull();
        assertThat(dbItem.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(2));
    }
    
    @Test
    void addItemToCart_existingItem_updatesQuantity() {
        AddItemRequestDTO existingItemDto = new AddItemRequestDTO(BigDecimal.valueOf(2), BigDecimal.TEN, currency); 

        ShoppingCart returnedCart = given()
            .contentType(ContentType.JSON)
            .body(existingItemDto)
        .when()
            .post("/{userId}/{asin}", userId1, asin1) // Existing ASIN for user1
        .then()
            .statusCode(HttpStatus.OK.value())
            .extract().as(ShoppingCart.class);
            
        assertThat(returnedCart.getUserId()).isEqualTo(userId1);
        assertThat(returnedCart.getAsin()).isEqualTo(asin1);
        // Initial quantity was 1, added 2, so should be 3
        assertThat(returnedCart.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(3));
        
        // Verify in DB
        ShoppingCart dbItem = shoppingCartRepository.findByUserIdAndAsin(userId1, asin1).orElse(null);
        assertThat(dbItem).isNotNull();
        assertThat(dbItem.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(3));
    }

    @Test
    void updateItemQuantity_updatesAndReturnsItem() {
        UpdateCartItemQtyDTO updateDto = new UpdateCartItemQtyDTO(BigDecimal.valueOf(5));

        ShoppingCart returnedCart = given()
            .contentType(ContentType.JSON)
            .body(updateDto)
        .when()
            .put("/{userId}/{asin}", userId1, asin1)
        .then()
            .statusCode(HttpStatus.OK.value())
            .extract().as(ShoppingCart.class);

        assertThat(returnedCart.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(5));
        
        // Verify in DB
        ShoppingCart dbItem = shoppingCartRepository.findByUserIdAndAsin(userId1, asin1).orElse(null);
        assertThat(dbItem).isNotNull();
        assertThat(dbItem.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(5));
    }
    
    @Test
    void updateItemQuantity_toZero_removesItemAndReturnsNoContent() {
        UpdateCartItemQtyDTO updateDto = new UpdateCartItemQtyDTO(BigDecimal.ZERO);

        given()
            .contentType(ContentType.JSON)
            .body(updateDto)
        .when()
            .put("/{userId}/{asin}", userId1, asin1)
        .then()
            .statusCode(HttpStatus.NO_CONTENT.value()); 

        // Verify in DB
        assertThat(shoppingCartRepository.findByUserIdAndAsin(userId1, asin1)).isEmpty();
    }


    @Test
    void removeItemFromCart_removesAndReturnsNoContent() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .delete("/{userId}/{asin}", userId1, asin1)
        .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // Verify in DB
        assertThat(shoppingCartRepository.findByUserIdAndAsin(userId1, asin1)).isEmpty();
    }
    
    // Test for clearing the whole cart for a user
    @Test
    void clearCart_removesAllItemsForUserAndReturnsNoContent() {
        // Add another item to the cart for user1 to ensure clearCart works for multiple items
        ShoppingCart cartItem2User1 = new ShoppingCart(userId1, asin2, BigDecimal.valueOf(5), BigDecimal.valueOf(5), BigDecimal.ONE, currency);
        shoppingCartRepository.save(cartItem2User1);
        
        assertThat(shoppingCartRepository.findByUserId(userId1)).hasSize(2); // Verify two items exist

        given()
            .contentType(ContentType.JSON)
        .when()
            .delete("/{userId}", userId1)
        .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // Verify in DB
        assertThat(shoppingCartRepository.findByUserId(userId1)).isEmpty();
    }

    // Test case for updating quantity of a non-existent item (should result in 404)
    @Test
    void updateItemQuantity_itemNotFound_returnsNotFound() {
        UpdateCartItemQtyDTO updateDto = new UpdateCartItemQtyDTO(BigDecimal.valueOf(5));
        String nonExistentAsin = "ASINNONEXIST";

        given()
            .contentType(ContentType.JSON)
            .body(updateDto)
        .when()
            .put("/{userId}/{asin}", userId1, nonExistentAsin)
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value()); // Assuming OrderNotFoundException leads to 404
    }

    // Test case for removing a non-existent item (should result in 404)
    @Test
    void removeItemFromCart_itemNotFound_returnsNotFound() {
        String nonExistentAsin = "ASINNONEXIST";
        
        given()
            .contentType(ContentType.JSON)
        .when()
            .delete("/{userId}/{asin}", userId1, nonExistentAsin)
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value()); // Assuming OrderNotFoundException leads to 404
    }
}
```
