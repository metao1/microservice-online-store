package com.metao.book.order.presentation;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.metao.book.order.infrastructure.persistence.cart.ShoppingCartJpaEntity;
import com.metao.book.order.infrastructure.persistence.cart.SpringDataShoppingCartRepository;
import com.metao.book.order.presentation.dto.AddItemRequestDto;
import com.metao.book.order.presentation.dto.UpdateCartItemQtyDto;
import com.metao.book.shared.test.KafkaContainer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@ActiveProfiles("test")
@TestPropertySource(properties = "kafka.enabled=true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShoppingCartControllerIT extends KafkaContainer {

    @LocalServerPort
    private Integer port;

    @Autowired
    private SpringDataShoppingCartRepository shoppingCartRepository;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private final String userId1 = "user123";
    private final String userToken = "mock-jwt-token-user";

    private final String sku1 = "SKU001";
    private final String sku2 = "SKU002";
    private final String productTitle = "product123";

    private Currency currency;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        shoppingCartRepository.deleteAll();
        currency = Currency.getInstance("EUR");

        ShoppingCartJpaEntity cartItem1User1 = new ShoppingCartJpaEntity(userId1, sku1, productTitle, BigDecimal.TEN, BigDecimal.TEN,
            BigDecimal.ONE, currency);
        shoppingCartRepository.save(cartItem1User1);
    }

    @AfterEach
    void tearDown() {
        shoppingCartRepository.deleteAll();
    }

    @Test
    void getCart_whenCartExists_returnsCart() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + userToken)
            .when()
            .get("/cart")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("shopping_cart_items", hasSize(1))
            .body("shopping_cart_items[0].sku", equalTo(sku1))
            .body("shopping_cart_items[0].quantity", is(1))
            .body("shopping_cart_items[0].price", is(10));
    }

    @Test
    void getCart_whenCartDoesNotExist_returnsEmptyCart() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + userToken)
            .when()
            .get("/cart")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("shopping_cart_items", empty());
    }

    @Test
    void addItemToCart_newItem_returnsCreatedItem() {
        var newItemDto = List.of(
            new AddItemRequestDto.Item(sku2, productTitle, BigDecimal.TWO, BigDecimal.valueOf(20.0), currency)
        );

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + userToken)
            .body(newItemDto)
            .when()
            .post("/cart/items")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .body(equalTo("1"));

        ShoppingCartJpaEntity dbItem = shoppingCartRepository.findByUserIdAndSku(userId1, sku2).orElse(null);
        assertThat(dbItem).isNotNull();
        assertThat(dbItem.getQuantity()).isEqualByComparingTo(BigDecimal.TWO);
    }

    @Test
    void addItemToCart_existingItem_updatesQuantity() {
        var existingItemDto = List.of(
            new AddItemRequestDto.Item(sku1, productTitle, BigDecimal.TWO, BigDecimal.TEN, currency)
        );

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + userToken)
            .body(existingItemDto)
            .when()
            .post("/cart/items")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .body(equalTo("1"));

        ShoppingCartJpaEntity dbItem = shoppingCartRepository.findByUserIdAndSku(userId1, sku1).orElse(null);
        assertThat(dbItem).isNotNull();
        assertThat(dbItem.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(3));
    }

    @Test
    void updateItemQuantity_toZero_removesItemAndReturnsNoContent() {
        UpdateCartItemQtyDto updateDto = new UpdateCartItemQtyDto(BigDecimal.ZERO);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + userToken)
            .body(updateDto)
            .when()
            .put("/cart/items/{sku}", sku1)
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(shoppingCartRepository.findByUserIdAndSku(userId1, sku1)).isEmpty();
    }

    @Test
    void removeItemFromCart_removesAndReturnsNoContent() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + userToken)
            .when()
            .delete("/cart/items/{sku}", sku1)
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(shoppingCartRepository.findByUserIdAndSku(userId1, sku1)).isEmpty();
    }

    @Test
    void clearCart_removesAllItemsForUserAndReturnsNoContent() {
        ShoppingCartJpaEntity cartItem2User1 = new ShoppingCartJpaEntity(userId1, sku2,
            productTitle, BigDecimal.valueOf(5), BigDecimal.valueOf(5),
            BigDecimal.ONE, currency);
        shoppingCartRepository.save(cartItem2User1);

        assertThat(shoppingCartRepository.findByUserId(userId1)).hasSize(2);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + userToken)
            .when()
            .delete("/cart")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(shoppingCartRepository.findByUserId(userId1)).isEmpty();
    }

    @Test
    void updateItemQuantity_itemNotFound_returnsNotFound() {
        UpdateCartItemQtyDto updateDto = new UpdateCartItemQtyDto(BigDecimal.valueOf(5));
        String nonExistentSku = "SKUNONEXIST";

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + userToken)
            .body(updateDto)
            .when()
            .put("/cart/items/{sku}", nonExistentSku)
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void removeItemFromCart_itemNotFound_returnsNotFound() {
        String nonExistentSku = "SKUNONEXIST";

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + userToken)
            .when()
            .delete("/cart/items/{sku}", nonExistentSku)
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void getCart_withoutAuth_returnsUnauthorized() {
        given()
            .contentType(ContentType.JSON)
            .when()
            .get("/cart")
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void addItemToCart_withoutAuth_returnsUnauthorized() {
        var newItemDto = List.of(
            new AddItemRequestDto.Item(sku2, productTitle, BigDecimal.TWO, BigDecimal.valueOf(20.0), currency)
        );

        given()
            .contentType(ContentType.JSON)
            .body(newItemDto)
            .when()
            .post("/cart/items")
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.value());
    }
}
