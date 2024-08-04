package com.metao.book.order.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.order.BaseKafkaIT;
import com.metao.book.order.OrderCreatedEvent;
import com.metao.book.order.OrderTestConstant;
import com.metao.book.order.OrderTestUtil;
import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.application.service.OrderMapper;
import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.domain.OrderStatus;
import com.metao.book.order.infrastructure.repository.OrderRepository;
import com.metao.book.shared.test.StreamBuilderTestUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Order(2)
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
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

    @BeforeEach
    public void setup() {
        if (orderRepository.count() == 0) {
            // Create a list of dummy orders for the mocked page
            List<OrderEntity> orders = OrderTestUtil.buildDummyOrders(10);
            orderRepository.saveAllAndFlush(orders);
            assertEquals(10, orderRepository.findAll().size());
        }
    }

    @Test
    @SneakyThrows
    @DisplayName("When Create an order then it is OK and return the order")
    void createOrderIsOk() {
        var createOrderDTO = OrderDTO.builder()
                .customerId(OrderTestConstant.CUSTOMER_ID)
            .productId(OrderTestConstant.PRODUCT_ID)
                .currency(OrderTestConstant.EUR.toString())
            .quantity(OrderTestConstant.QUANTITY)
            .price(OrderTestConstant.PRICE)
            .build();

        mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(StreamBuilderTestUtils.convertObjectToJsonBytes(objectMapper, createOrderDTO)))
                .andExpect(status().isOk());

    }

    @Test
    @SneakyThrows
    @DisplayName("When Get an order then it is OK and return the order")
    void getOrderIsOK() {
        var expectedOrder = OrderCreatedEvent.newBuilder()
            .setId(OrderTestConstant.ORDER_ID)
                .setCustomerId(OrderTestConstant.CUSTOMER_ID)
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
                .productId(OrderTestConstant.PRODUCT_ID)
                .currency(OrderTestConstant.EUR.toString())
                .quantity(OrderTestConstant.QUANTITY)
                .price(OrderTestConstant.PRICE)
                .build();

        mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(StreamBuilderTestUtils.convertObjectToJsonBytes(objectMapper, createOrderDTO)))
                .andExpect(status().isBadRequest())
                .andDo(result -> verify(orderRepository, never()).save(any(OrderEntity.class)));
    }

    @Test
    @SneakyThrows
    @DisplayName("Get orders by product ids and statuses")
    void getOrderByProductIdsAndStatusesPageable() {
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
