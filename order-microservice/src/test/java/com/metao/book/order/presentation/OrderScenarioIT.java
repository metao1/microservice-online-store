package com.metao.book.order.presentation;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.order.application.dto.CreateOrderDTO;
import com.metao.book.order.application.dto.OrderCreatedEvent;
import com.metao.book.order.application.service.OrderMapper;
import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.infrastructure.repository.OrderRepository;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.order.OrderStatus;
import com.metao.book.shared.test.StreamBuilderTestUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@Slf4j
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles({"test", "container"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderScenarioIT extends BaseKafkaIT {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final String ACCOUNT_ID = "ACCOUNT_ID";
    private static final String PRODUCT_ID = "1234567892";
    private static final BigDecimal QUANTITY = BigDecimal.valueOf(100d);
    private static final BigDecimal PRICE = BigDecimal.valueOf(123d);
    private static final String ORDER_ID = "ORDER_ID";

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    OrderMapper mapper;

    @Autowired
    KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @SpyBean
    OrderRepository orderRepository;

    @BeforeAll
    public void setup() {
        super.setup();
        // Create a list of dummy orders for the mocked page
        List<OrderEntity> orders = generateDummyOrders(10);

        orderRepository.saveAllAndFlush(orders);
        assertEquals(10, orderRepository.findAll().size());
    }

    @AfterAll
    public void tearDown() {
        orderRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void createOrderIsOk() {
        var createOrderDTO = CreateOrderDTO.builder()
            .accountId(ACCOUNT_ID)
            .productId(PRODUCT_ID)
            .currency(EUR)
            .quantity(QUANTITY)
            .price(PRICE)
            .build();

        mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(StreamBuilderTestUtils.convertObjectToJsonBytes(objectMapper, createOrderDTO)))
            .andExpect(status().isOk());

        // Wait for the asynchronous operation to finish
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> verify(orderRepository).save(any(OrderEntity.class)));
    }

    @Test
    @SneakyThrows
    void getOrderIsOK() {
        var expectedOrder = OrderCreatedEvent.builder()
            .orderId(ORDER_ID)
            .customerId(ACCOUNT_ID)
            .productId(PRODUCT_ID)
            .currency(EUR)
            .status(OrderStatus.NEW)
            .price(PRICE)
            .quantity(QUANTITY)
            .build();

        var expectedOrderEntity = mapper.toEntity(expectedOrder);

        when(orderRepository.findById(new OrderId(ORDER_ID)))
            .thenReturn(Optional.of(expectedOrderEntity));

        mockMvc.perform(get("/order").param("order_id", ORDER_ID))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void getUnknownOrderIsNotFound() {
        var unknownOrderId = "UNKNOWN_ORDER_ID";

        mockMvc.perform(get("/order").param("order_id", unknownOrderId))
            .andExpect(status().isNotFound());
    }


    @Test
    @SneakyThrows
    void createInvalidOrderRequestIsBadRequest() {
        var createOrderDTO = CreateOrderDTO.builder()
            .productId(PRODUCT_ID)
            .currency(EUR)
            .quantity(QUANTITY)
            .price(PRICE)
            .build();

        mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(StreamBuilderTestUtils.convertObjectToJsonBytes(objectMapper, createOrderDTO)))
            .andExpect(status().isBadRequest());

        // Wait for the asynchronous operation to finish
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> verify(orderRepository, never()).save(any(OrderEntity.class)));
    }

    @Test
    void getOrderByProductIdsAndStatusesPageable() throws Exception {
        // Perform GET request for the first page
        for (int i = 0; i < 2; i++) {
            mockMvc
                .perform(
                    MockMvcRequestBuilders.get("/order/{pageSize}/{offset}/{sortByFieldName}", 5, i, "status")
                )
                .andExpect(status().isOk())
                .andExpect(
                    jsonPath("$.content").exists()) // Verify the presence of "content" field in the JSON response
                .andExpect(
                    jsonPath(
                        "$.content[*].status").exists()) // Verify the presence of "order_id" field in each OrderDTO
                .andExpect(jsonPath("$.numberOfElements").value(5)); // Verify that the number of returned items is 5
        }
    }

    @Test
    @SneakyThrows
    void getOrderProductIdsAndStatusWithCriteria() {
        // Mock data
        Set<String> productIds = new HashSet<>();
        productIds.add("product_1");
        productIds.add("product_2");

        Set<String> statuses = new HashSet<>();
        statuses.add(OrderStatus.NEW.toString());

        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/order/{pageSize}/{offset}/{sortByFieldName}", 5, 0, "productId")
                    .queryParam("productIds", String.join(",", productIds))
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.content").exists()) // Verify the presence of "content" field in the JSON response
            .andExpect(jsonPath("$.numberOfElements").value(2)); // Verify that the number of returned items is 2

        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/order/{pageSize}/{offset}/{sortByFieldName}", 5, 0, "productId")
                    .queryParam("statuses", String.join(",", statuses))
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.content").exists()) // Verify the presence of "content" field in the JSON response
            .andExpect(jsonPath("$.numberOfElements").value(5)); // Verify that the number of returned items is 3

        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/order/{pageSize}/{offset}/{sortByFieldName}", 5, 0, "productId")
                    .queryParam("productIds", String.join(",", productIds))
                    .queryParam("statuses", String.join(",", statuses))
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.content").exists()) // Verify the presence of "content" field in the JSON response
            .andExpect(jsonPath("$.numberOfElements").value(1)); // Verify that the number of returned items is 3
    }

    public static List<OrderEntity> generateDummyOrders(int count) {
        List<OrderEntity> orders = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String orderId = UUID.randomUUID().toString(); // Generate a random UUID as order ID
            String customerId = "customer_" + i; // Generate a dummy customer ID
            String productId = "product_" + i; // Generate a dummy product ID
            BigDecimal productCount = BigDecimal.valueOf(i + 1); // Incremental product count
            Money money = new Money(EUR, BigDecimal.valueOf((i + 1) * 10L)); // Dummy money
            OrderStatus orderStatus =
                i % 2 == 0 ? OrderStatus.NEW : OrderStatus.SUBMITTED; // Set status to NEW for all orders
            OrderEntity order = new OrderEntity(orderId, customerId, productId, productCount, money, orderStatus);
            orders.add(order);
        }
        return orders;
    }
}
