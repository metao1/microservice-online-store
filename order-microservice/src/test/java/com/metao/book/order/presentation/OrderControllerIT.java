package com.metao.book.order.presentation;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.metao.book.order.application.cart.ShoppingCartDto;
import com.metao.book.order.application.cart.ShoppingCartItem;
import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.CustomerId;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.repository.OrderRepository;
import com.metao.book.order.presentation.dto.AddItemRequestDto;
import com.metao.book.order.presentation.dto.CreateOrderRequest;
import com.metao.book.order.presentation.dto.UpdateStatusRequestDto;
import com.metao.shared.test.KafkaContainer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for OrderController
 */
@ActiveProfiles("test")
@DisplayName("OrderController Integration Tests")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerIT extends KafkaContainer {

    private final String userId = "e2eUser";
    private final String sku = "SKU_E2E_001"; // Assume this product exists in inventory-microservice
    private final String sku1 = "SKU_E2E_001"; // Assume this product exists in inventory-microservice
    private final BigDecimal price1 = BigDecimal.valueOf(12.99);
    private final Currency currency = Currency.getInstance("EUR");

    @LocalServerPort
    private Integer port;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    KafkaTemplate<String, Objects> eventPublisher;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Nested
    @DisplayName("Order Creation")
    class OrderCreation {

        @Test
        @DisplayName("Should create order successfully")
        void shouldCreateOrderSuccessfully() {
            // Given
            CreateOrderRequest request = new CreateOrderRequest();
            request.setCustomerId("customer123");
            var cart = new ShoppingCartDto(userId,
                Set.of(new ShoppingCartItem(sku, 1, new BigDecimal(100), Currency.getInstance("USD")))
            );

            // When & Then
            given()
                .contentType(ContentType.JSON)
                .body(request)
                .post("/api/orders")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("value", matchesPattern("[a-f0-9\\-]+"));// UUID generated orderId in the service
        }

        @Test
        @DisplayName("Should add item to orders successfully")
        void shouldAddItemToOrdersSuccessfully() {
            // GIVEN
            AddItemRequestDto request = new AddItemRequestDto(sku1, 1, price1, currency);

            var orderId = new OrderId("order123");

            var cart = new ShoppingCartDto(userId,
                Set.of(new ShoppingCartItem(sku, 1, new BigDecimal(100), Currency.getInstance("USD")))
            );
            /*when(shoppingCartService.getCartForUser("customer123"))
                .thenReturn(cart);*/
            OrderAggregate orderAggregate = new OrderAggregate(orderId, new CustomerId("customer123"));

            when(orderRepository.save(any(OrderAggregate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(orderAggregate));

            given()
                .contentType(ContentType.JSON)
                .body(request)
                .put("/api/orders/{orderId}/items", orderId.value())
                .then()
                .statusCode(HttpStatus.OK.value());

        }

        @Test
        @DisplayName("Process updating of an order to different status successfully")
        void shouldUpdateStatusOfAnOrderSuccessfully() {
            // GIVEN
            // Should update from CREATED to PAID successfully
            UpdateStatusRequestDto paidStatusUpdate = new UpdateStatusRequestDto("PAID");
            var orderId = new OrderId("order123");

            OrderAggregate orderAggregate = new OrderAggregate(orderId, new CustomerId("customer123"));

            when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(orderAggregate));
            when(orderRepository.save(any(OrderAggregate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            given()
                .contentType(ContentType.JSON)
                .body(paidStatusUpdate)
                .patch("/api/orders/{orderId}/status", orderId.value())
                .then()
                .statusCode(HttpStatus.OK.value());

            // Should NOT able to update from PAID to CANCEL
            UpdateStatusRequestDto cancelStatusUpdate = new UpdateStatusRequestDto("PAID");
            given()
                .contentType(ContentType.JSON)
                .body(cancelStatusUpdate)
                .patch("/api/orders/{orderId}/status", orderId.value())
                .then()
                .statusCode(HttpStatus.NOT_ACCEPTABLE.value());
        }
    }

    @Nested
    @DisplayName("Order Item Management")
    class OrderItemManagement {

        @Test
        @DisplayName("Should add items to order successfully")
        void shouldAddItemToOrderSuccessfully() {
            // Given
            var orderId = new OrderId("order123");
            AddItemRequestDto addItemRequestDto = new AddItemRequestDto(sku, 1, price1, currency);
            OrderAggregate orderAggregate = new OrderAggregate(orderId, new CustomerId("customer123"));
            orderRepository.save(orderAggregate);

            // When & Then
            given()
                .contentType(ContentType.JSON)
                .body(addItemRequestDto)
                .post("/api/orders/{orderId}/items", orderId.value())
                .then()
                .statusCode(HttpStatus.CREATED.value());

            OrderAggregate updatedOrder = orderRepository.findById(orderId).orElseThrow();
            assertThat(updatedOrder.getDomainEvents())
                .size()
                .isEqualTo(2);

            eventPublisher.receive(
                "",
                1,
                0
            );
        }
    }
    /*
    @Nested
    @DisplayName("Order Status Management")
    class OrderStatusManagement {

        @Test
        @DisplayName("Should update order status successfully")
        void shouldUpdateOrderStatusSuccessfully() throws Exception {
            // Given
            UpdateStatusRequest request = new UpdateStatusRequest();
            request.setStatus("PAID");

            when(orderService.createOrder(any(CustomerId.class)))
                .thenReturn(testOrderId);

            // When & Then
            mockMvc.perform(patch("/api/orders/{orderId}/status", testOrderId.value())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Order Queries")
    class OrderQueries {

        @Test
        @DisplayName("Should get customer orders successfully")
        void shouldGetCustomerOrdersSuccessfully() throws Exception {
            // Given
            when(orderService.getCustomerOrders(any(CustomerId.class)))
                .thenReturn(List.of());

            // When & Then
            mockMvc.perform(get("/api/orders/customer/{customerId}", "customer123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

            verify(orderService).getCustomerOrders(any(CustomerId.class));
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should handle service exceptions gracefully")
        void shouldHandleServiceExceptionsGracefully() throws Exception {
            // Given
            CreateOrderRequest request = new CreateOrderRequest();
            request.setCustomerId("customer123");

            when(orderService.createOrder(any(CustomerId.class)))
                .thenThrow(new RuntimeException("Service error"));

            // When & Then
            // Since there's no global exception handler, the exception will be wrapped in ServletException
            // We verify that the exception is thrown and contains our original message
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should handle malformed JSON")
        void shouldHandleMalformedJson() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{invalid json}"))
                .andExpect(status().isBadRequest());
        }
    }*/
}
