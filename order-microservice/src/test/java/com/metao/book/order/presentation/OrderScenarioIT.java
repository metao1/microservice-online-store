package com.metao.book.order.presentation;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.OrderEventOuterClass;
import com.metao.book.order.application.dto.CreateOrderDTO;
import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.application.service.OrderMapper;
import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.domain.Status;
import com.metao.book.order.infrastructure.repository.OrderRepository;
import com.metao.book.shared.domain.financial.Currency;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.test.StreamBuilderTestUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
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
@ActiveProfiles({"test", "container"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderScenarioIT extends BaseKafkaIT {

    public static final String ACCOUNT_ID = "ACCOUNT_ID";
    public static final String PRODUCT_ID = "1234567892";
    private static final BigDecimal QUANTITY = BigDecimal.valueOf(100d);
    private static final BigDecimal PRICE = BigDecimal.valueOf(123d);
    public static final String ORDER_ID = "ORDER_ID";

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    OrderMapper mapper;

    @Autowired
    KafkaTemplate<String, OrderEventOuterClass.OrderEvent> kafkaTemplate;

    @SpyBean
    OrderRepository orderRepository;

    @Test
    @SneakyThrows
    void createOrderIsOk() {
        var createOrderDTO = CreateOrderDTO.builder()
            .accountId(ACCOUNT_ID)
            .productId(PRODUCT_ID)
            .currency(Currency.EUR)
            .quantity(QUANTITY)
            .price(PRICE)
            .build();

        mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(StreamBuilderTestUtils.convertObjectToJsonBytes(objectMapper, createOrderDTO)))
            .andExpect(status().isOk());

        // Wait for the asynchronous operation to finish
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(orderRepository).save(any(OrderEntity.class));
        });
    }

    @Test
    @SneakyThrows
    void getOrderIsOK() {
        var expectedOrder = OrderDTO.builder()
            .orderId(ORDER_ID)
            .customerId(ACCOUNT_ID)
            .productId(PRODUCT_ID)
            .currency(Currency.EUR)
            .status(Status.NEW)
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
    void getOrderByProductIdsAndStatusesPageable() throws Exception {
        // Create a list of dummy orders for the mocked page
        List<OrderEntity> orders = generateDummyOrders(10);

        orderRepository.saveAllAndFlush(orders);
        assertFalse(orderRepository.findAll().isEmpty());

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
        statuses.add(Status.NEW.toString());
        // Create a list of dummy orders for the mocked page
        List<OrderEntity> orders = generateDummyOrders(5);

        orderRepository.saveAllAndFlush(orders);

        assertFalse(orderRepository.findAll().isEmpty());

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
            .andExpect(jsonPath("$.numberOfElements").value(3)); // Verify that the number of returned items is 3

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
            Money money = new Money(Currency.EUR, BigDecimal.valueOf((i + 1) * 10L)); // Dummy money
            Status status = i % 2 == 0 ? Status.NEW : Status.SUBMITTED; // Set status to NEW for all orders
            OrderEntity order = new OrderEntity(orderId, customerId, productId, productCount, money, status);
            orders.add(order);
        }
        return orders;
    }

//    @SneakyThrows
//    @KafkaListener(id = "order-listener-test", topics = "${kafka.topic.name}", groupId = "order-grp")
//    @Transactional
//    public void onOrderListenerTest(
//        ConsumerRecord<String, String> record
//    ) {
//        final OrderDTO orderDTO;
//        try {
//            orderDTO = objectMapper.readValue(record.value(), OrderDTO.class);
//        } catch (JsonProcessingException e) {
//            log.error(e.getMessage());
//            throw new RuntimeException(e);
//        }
//        arrayBlockingQueue.put(orderDTO);
//    }

}
