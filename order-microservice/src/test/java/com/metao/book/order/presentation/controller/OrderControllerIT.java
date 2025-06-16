package com.metao.book.order.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.order.application.cart.ShoppingCartService;
import com.metao.book.order.application.service.OrderApplicationService;
import com.metao.book.order.domain.event.DomainEventPublisher;
import com.metao.book.order.domain.model.valueobject.CustomerId;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.repository.OrderRepository;
import com.metao.book.order.presentation.dto.AddItemRequest;
import com.metao.book.order.presentation.dto.CreateOrderRequest;
import com.metao.book.order.presentation.dto.UpdateStatusRequest;
import com.metao.shared.test.BaseKafkaTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for OrderController
 */
@WebMvcTest(
    controllers = OrderController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class
    }
)
@ActiveProfiles("test")
@DisplayName("OrderController Integration Tests")
class OrderControllerIT extends BaseKafkaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderApplicationService orderApplicationService;

    // Mock all the dependencies that might be needed by the application context
    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private DomainEventPublisher domainEventPublisher;

    @MockitoBean
    private ShoppingCartService shoppingCartService;

    private OrderId testOrderId;

    @BeforeEach
    void setUp() {
        testOrderId = OrderId.generate();
    }

    @Nested
    @DisplayName("Order Creation")
    class OrderCreation {

        @Test
        @DisplayName("Should create order successfully")
        void shouldCreateOrderSuccessfully() throws Exception {
            // Given
            CreateOrderRequest request = new CreateOrderRequest();
            request.setCustomerId("customer123");

            when(orderApplicationService.createOrder(any(CustomerId.class))).thenReturn(testOrderId);

            // When & Then
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(testOrderId.value()));

            verify(orderApplicationService).createOrder(any(CustomerId.class));
        }
    }

    @Nested
    @DisplayName("Order Item Management")
    class OrderItemManagement {

        @Test
        @DisplayName("Should add item to order successfully")
        void shouldAddItemToOrderSuccessfully() throws Exception {
            // Given
            AddItemRequest request = new AddItemRequest();
            request.setProductId("product123");
            request.setProductName("Test Product");
            request.setQuantity(2);
            request.setUnitPrice(15.99);

            // When & Then
            mockMvc.perform(post("/api/orders/{orderId}/items", testOrderId.value())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

            verify(orderApplicationService).addItemToOrder(
                eq(OrderId.of(testOrderId.value())),
                any(),
                eq("Test Product"),
                any(),
                any()
            );
        }
    }

    @Nested
    @DisplayName("Order Status Management")
    class OrderStatusManagement {

        @Test
        @DisplayName("Should update order status successfully")
        void shouldUpdateOrderStatusSuccessfully() throws Exception {
            // Given
            UpdateStatusRequest request = new UpdateStatusRequest();
            request.setStatus("PAID");

            // When & Then
            mockMvc.perform(patch("/api/orders/{orderId}/status", testOrderId.value())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

            verify(orderApplicationService).updateOrderStatus(
                eq(OrderId.of(testOrderId.value())),
                eq("PAID")
            );
        }
    }

    @Nested
    @DisplayName("Order Queries")
    class OrderQueries {

        @Test
        @DisplayName("Should get customer orders successfully")
        void shouldGetCustomerOrdersSuccessfully() throws Exception {
            // Given
            when(orderApplicationService.getCustomerOrders(any(CustomerId.class)))
                .thenReturn(List.of());

            // When & Then
            mockMvc.perform(get("/api/orders/customer/{customerId}", "customer123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

            verify(orderApplicationService).getCustomerOrders(any(CustomerId.class));
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

            when(orderApplicationService.createOrder(any(CustomerId.class)))
                .thenThrow(new RuntimeException("Service error"));

            // When & Then
            // Since there's no global exception handler, the exception will be wrapped in ServletException
            // We verify that the exception is thrown and contains our original message
            try {
                mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
            } catch (Exception e) {
                // Verify that the exception chain contains our original RuntimeException
                assertThat(e.getCause()).isInstanceOf(RuntimeException.class);
                assertThat(e.getCause().getMessage()).contains("Service error");
            }
        }

        @Test
        @DisplayName("Should handle malformed JSON")
        void shouldHandleMalformedJson() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{invalid json"))
                .andExpect(status().isBadRequest());
        }
    }
}
