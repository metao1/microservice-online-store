package com.metao.book.order.presentation;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.metao.book.order.application.cart.ShoppingCartItem;
import com.metao.book.order.infrastructure.persistence.cart.ShoppingCartJpaEntity;
import com.metao.book.order.infrastructure.persistence.cart.ShoppingCartRepository;
import com.metao.book.order.presentation.dto.AddItemRequestDto;
import com.metao.book.order.presentation.dto.UpdateCartItemQtyDto;
import com.metao.book.outbox.infrastructure.OutboxKafkaPublisher;
import com.metao.shared.test.KafkaContainerBase;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import org.mockito.Mockito;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test")
@TestPropertySource(properties = "kafka.enabled=true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShoppingCartControllerIT extends KafkaContainerBase {

    private static final String USER_ID = "user_secure_123";

    @LocalServerPort
    private Integer port;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private OutboxKafkaPublisher outboxKafkaPublisher;

    private final String userId1 = "user123";
    private final String userToken = "mock-jwt-token-user";

    private final String sku1 = "SKU001";
    private final String sku2 = "SKU002";
    private final String productTitle = "product123";
    private final BigDecimal quantity = BigDecimal.ONE;
    private final BigDecimal price = BigDecimal.valueOf(12.99);
    private final Currency currency = Currency.getInstance("EUR");

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        Mockito.when(jwtDecoder.decode(userToken)).thenReturn(
            Jwt.withTokenValue(userToken)
                .header("alg", "none")
                .subject(userId1)
                .audience(List.of("account"))
                .claim("roles", List.of("CUSTOMER"))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build()
        );
        shoppingCartRepository.deleteAll();
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
        shoppingCartRepository.deleteAll();
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
            new ShoppingCartItem(sku2, productTitle, BigDecimal.TWO, BigDecimal.valueOf(20.0), currency)
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
    void addItemToCart_ignoresCallerSuppliedUserId() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + userToken)
            .body("""
                [{
                  "user_id": "victim-user",
                  "sku": "SKU002",
                  "productTitle": "product123",
                  "quantity": 2,
                  "price": 20.0,
                  "currency": "EUR"
                }]
                """)
            .when()
            .post("/cart/items")
            .then()
            .statusCode(HttpStatus.CREATED.value());

        assertThat(shoppingCartRepository.findByUserIdAndSku(userId1, sku2)).isPresent();
        assertThat(shoppingCartRepository.findByUserIdAndSku("victim-user", sku2)).isEmpty();
    }

    @Test
    void addItemToCart_existingItem_updatesQuantity() {
        var existingItemDto = List.of(
            new ShoppingCartItem(sku1, productTitle, BigDecimal.TWO, BigDecimal.TEN, currency)
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
        AddItemRequestDto addItemDTO = new AddItemRequestDto(
            USER_ID, List.of(new ShoppingCartItem(sku1, productTitle, quantity, price, currency))
        );


        given()
            .contentType(ContentType.JSON)
            .body(addItemDTO)
            .when()
            .post("/cart/items")
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.value());
    }
}
