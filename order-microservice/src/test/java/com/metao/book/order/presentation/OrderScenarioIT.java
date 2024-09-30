package com.metao.book.order.presentation;

import static com.metao.book.order.OrderTestConstant.CUSTOMER_ID;
import static com.metao.book.order.OrderTestConstant.EUR;
import static com.metao.book.order.OrderTestConstant.PRICE;
import static com.metao.book.order.OrderTestConstant.PRODUCT_ID;
import static com.metao.book.order.OrderTestConstant.QUANTITY;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.order.BaseKafkaIT;
import com.metao.book.order.OrderCreatedEvent;
import com.metao.book.order.OrderTestUtil;
import com.metao.book.order.application.card.OrderRepository;
import com.metao.book.order.domain.OrderDTO;
import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderMapper;
import com.metao.book.order.domain.OrderStatus;
import com.metao.book.shared.test.StreamBuilderTestUtils;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderScenarioIT extends BaseKafkaIT {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    OrderMapper mapper;

    @Autowired
    KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @Autowired
    OrderRepository orderRepository;

    @BeforeAll
    public void setup() {
        orderRepository.deleteAll();
        // Create a list of dummy orders for the mocked page
        List<OrderEntity> orders = OrderTestUtil.buildDummyOrders(10);
        orderRepository.saveAllAndFlush(orders);
        assertEquals(10, orderRepository.findAll().size());
    }

    @Test
    @SneakyThrows
    @DisplayName("When Create an order then it is OK and return the order")
    void createOrderIsOk() {
        var createOrderDTO = OrderDTO.builder()
            .customerId(CUSTOMER_ID)
            .productId(PRODUCT_ID)
            .currency(EUR.toString())
            .quantity(QUANTITY)
            .price(PRICE)
            .build();

        mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(StreamBuilderTestUtils.convertObjectToJsonBytes(objectMapper, createOrderDTO)))
            .andExpect(status().isOk())
            .andDo(result -> await().atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> assertEquals(11, orderRepository.findAll().size())));
    }

    @Test
    @SneakyThrows
    @DisplayName("When Get an order then it is OK and return the order")
    void getOrderIsOK() {
        var expectedOrder = OrderCreatedEvent.newBuilder()
            .setCustomerId(CUSTOMER_ID)
            .setProductId(PRODUCT_ID)
            .setCurrency(EUR.toString())
            .setStatus(OrderCreatedEvent.Status.NEW)
            .setPrice(PRICE.doubleValue())
            .setQuantity(QUANTITY.doubleValue())
            .build();

        var expectedOrderEntity = mapper.toEntity(expectedOrder);

        OrderEntity save = orderRepository.save(expectedOrderEntity);

        mockMvc.perform(get("/order").param("order_id", save.getOrderId()))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    @DisplayName("When Get an unknown order then it is not found")
    void getUnknownOrderIsNotFound() {
        var unknownOrderId = "UNKNOWN_ORDER_ID";

        mockMvc.perform(get("/order").param("order_id", unknownOrderId))
            .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    @DisplayName("When Create an invalid order then it is bad request")
    void createInvalidOrderRequestIsBadRequest() {
        var createOrderDTO = OrderDTO.builder()
            .productId(PRODUCT_ID)
            .currency(EUR.toString())
            .quantity(QUANTITY)
            .price(PRICE)
            .build();

        mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(StreamBuilderTestUtils.convertObjectToJsonBytes(objectMapper, createOrderDTO)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    @DisplayName("Get orders by product ids and statuses")
    void getOrderByProductIdsAndStatusesPageable() {
        // Perform GET request for the first page
        for (int i = 0; i < 2; i++) {
            mockMvc
                .perform(MockMvcRequestBuilders.get("/order/{pageSize}/{offset}/{sortByFieldName}", 5, i, "status"))
                .andExpect(status().isOk())
                .andExpect(
                    jsonPath("$.content").exists()) // Verify the presence of "content" field in the JSON response
                .andExpect(jsonPath(
                    "$.content[*].status").exists()) // Verify the presence of "order_id" field in each OrderDTO
                .andExpect(jsonPath("$.numberOfElements").value(5)); // Verify that the number of returned items is 5
        }
    }

    @Test
    @SneakyThrows
    @DisplayName("When Get orders then it is OK and return the orders")
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

}
