package com.metao.book.order.presentation;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.metao.book.order.application.cart.ShoppingCart;
import com.metao.book.order.application.cart.ShoppingCartRepository;
import com.metao.book.order.application.cart.UpdateCartItemQtyDTO;
import com.metao.book.order.presentation.dto.AddItemRequestDto;
import com.metao.shared.test.KafkaContainer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("test")
@TestPropertySource(properties = "kafka.enabled=true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShoppingCartControllerIT extends KafkaContainer {

    @LocalServerPort
    private Integer port;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    private String userId1 = "user123";

    private String sku1 = "SKU001";

    private String sku2 = "SKU002";

    private Currency currency;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/cart"; // Base path for ShoppingCartController

        shoppingCartRepository.deleteAll(); // Clean up before each test

        userId1 = "user123";
        sku1 = "SKU001";
        sku2 = "SKU002";
        currency = Currency.getInstance("USD");

        // Initial item for user1
        // Constructor: public ShoppingCart(String userId, String sku, BigDecimal buyPrice, BigDecimal sellPrice, BigDecimal quantity, Currency currency)
        ShoppingCart cartItem1User1 = new ShoppingCart(userId1, sku1, BigDecimal.TEN, BigDecimal.TEN, 1,
            currency);
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
            .body("shopping_cart_items[0].sku", equalTo(sku1))
            // Using closeTo for BigDecimal comparisons with Hamcrest for robustness
            .body("shopping_cart_items[0].quantity", is(1))
            .body("shopping_cart_items[0].price", is(10));
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

        AddItemRequestDto newItemDto = new AddItemRequestDto(sku1, 2, BigDecimal.valueOf(20.00), currency);

        given()
            .contentType(ContentType.JSON)
            .body(newItemDto)
        .when()
            .post("/{userId}/{sku}", userId1, sku2) // New SKU for user1
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("user_id", equalTo(userId1))
            .body("shopping_cart_items[0].sku", equalTo(sku2))
            .body("shopping_cart_items[0].quantity", equalTo(2))
            .body("shopping_cart_items[0].price", equalTo(20.0F))
            .body("shopping_cart_items[0].currency", equalTo(currency.toString()));
        
        // Verify in DB
        ShoppingCart dbItem = shoppingCartRepository.findByUserIdAndSku(userId1, sku2).orElse(null);
        assertThat(dbItem).isNotNull();
        assertThat(dbItem.getQuantity()).isEqualByComparingTo(2);
    }
    
    @Test
    void addItemToCart_existingItem_updatesQuantity() {
        AddItemRequestDto existingItemDto = new AddItemRequestDto(sku1, 2, BigDecimal.TEN, currency);

        given()
            .contentType(ContentType.JSON)
            .body(existingItemDto)
        .when()
            .post("/{userId}/{sku}", userId1, sku1) // Existing SKU for user1
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("user_id", equalTo(userId1))
            .body("shopping_cart_items[0].sku", equalTo(sku1))
            .body("shopping_cart_items[0].quantity", equalTo(3))
            .body("shopping_cart_items[0].price", equalTo(10))
            .body("shopping_cart_items[0].currency", equalTo(currency.toString()));

        // Verify in DB
        ShoppingCart dbItem = shoppingCartRepository.findByUserIdAndSku(userId1, sku1).orElse(null);
        assertThat(dbItem).isNotNull();
        assertThat(dbItem.getQuantity()).isEqualByComparingTo(3);
    }
    
    @Test
    void updateItemQuantity_toZero_removesItemAndReturnsNoContent() {
        UpdateCartItemQtyDTO updateDto = new UpdateCartItemQtyDTO(0);

        given()
            .contentType(ContentType.JSON)
            .body(updateDto)
        .when()
            .put("/{userId}/{sku}", userId1, sku1)
        .then()
            .statusCode(HttpStatus.NO_CONTENT.value()); 

        // Verify in DB
        assertThat(shoppingCartRepository.findByUserIdAndSku(userId1, sku1)).isEmpty();
    }


    @Test
    void removeItemFromCart_removesAndReturnsNoContent() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .delete("/{userId}/{sku}", userId1, sku1)
        .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // Verify in DB
        assertThat(shoppingCartRepository.findByUserIdAndSku(userId1, sku1)).isEmpty();
    }
    
    // Test for clearing the whole cart for a user
    @Test
    void clearCart_removesAllItemsForUserAndReturnsNoContent() {
        // Add another item to the cart for user1 to ensure clearCart works for multiple items
        ShoppingCart cartItem2User1 = new ShoppingCart(userId1, sku2, BigDecimal.valueOf(5), BigDecimal.valueOf(5), 1, currency);
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
        UpdateCartItemQtyDTO updateDto = new UpdateCartItemQtyDTO(5);
        String nonExistentSku = "SKUNONEXIST";

        given()
            .contentType(ContentType.JSON)
            .body(updateDto)
        .when()
            .put("/{userId}/{sku}", userId1, nonExistentSku)
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value()); // Assuming OrderNotFoundException leads to 404
    }

    // Test case for removing a non-existent item (should result in 404)
    @Test
    void removeItemFromCart_itemNotFound_returnsNotFound() {
        String nonExistentSku = "SKUNONEXIST";
        
        given()
            .contentType(ContentType.JSON)
        .when()
            .delete("/{userId}/{sku}", userId1, nonExistentSku)
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value()); // Assuming OrderNotFoundException leads to 404
    }
}

