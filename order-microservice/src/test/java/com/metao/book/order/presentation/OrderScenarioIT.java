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
import com.metao.book.OrderCreatedEventOuterClass.OrderCreatedEvent;
import com.metao.book.order.BaseKafkaIT;
import com.metao.book.order.OrderTestConstant;
import com.metao.book.order.OrderTestUtil;
import com.metao.book.order.application.dto.CreateOrderDTO;
import com.metao.book.order.application.service.OrderMapper;
import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.infrastructure.repository.OrderRepository;
import com.metao.book.shared.domain.order.OrderStatus;
import com.metao.book.shared.test.StreamBuilderTestUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
        List<OrderEntity> orders = OrderTestUtil.buildDummyOrders(10);

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
            .accountId(OrderTestConstant.ACCOUNT_ID)
            .productId(OrderTestConstant.PRODUCT_ID)
            .currency(OrderTestConstant.EUR)
            .quantity(OrderTestConstant.QUANTITY)
            .price(OrderTestConstant.PRICE)
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
        var expectedOrder = OrderCreatedEvent.newBuilder()
            .setId(OrderTestConstant.ORDER_ID)
            .setCustomerId(OrderTestConstant.ACCOUNT_ID)
            .setProductId(OrderTestConstant.PRODUCT_ID)
            .setCurrency(OrderTestConstant.EUR.toString())
            .setStatus(OrderCreatedEvent.Status.NEW)
            .setPrice(OrderTestConstant.PRICE.doubleValue())
            .setQuantity(OrderTestConstant.QUANTITY.doubleValue())
            .build();

        var expectedOrderEntity = mapper.toEntity(expectedOrder);

        when(orderRepository.findById(new OrderId(OrderTestConstant.ORDER_ID)))
            .thenReturn(Optional.of(expectedOrderEntity));

        mockMvc.perform(get("/order").param("order_id", OrderTestConstant.ORDER_ID))
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
            .productId(OrderTestConstant.PRODUCT_ID)
            .currency(OrderTestConstant.EUR)
            .quantity(OrderTestConstant.QUANTITY)
            .price(OrderTestConstant.PRICE)
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
}
