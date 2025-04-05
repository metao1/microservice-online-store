package com.metao.book.order.presentation;

import static com.metao.book.order.OrderTestConstant.CUSTOMER_ID;
import static com.metao.book.order.OrderTestConstant.EUR;
import static com.metao.book.order.OrderTestConstant.PRICE;
import static com.metao.book.order.OrderTestConstant.PRODUCT_ID;
import static com.metao.book.order.OrderTestConstant.QUANTITY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.order.OrderCreatedEvent;
import com.metao.book.order.OrderTestUtil;
import com.metao.book.order.application.cart.OrderRepository;
import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderStatus;
import com.metao.book.order.domain.dto.OrderDTO;
import com.metao.book.order.infrastructure.kafka.KafkaOrderMapper;
import com.metao.book.shared.test.StreamBuilderTestUtils;
import com.metao.shared.test.BaseKafkaIT;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderScenarioIT extends BaseKafkaIT {

        @Autowired
        ObjectMapper objectMapper;

        @Autowired
        MockMvc mockMvc;

        @Autowired
        OrderRepository orderRepository;

        @BeforeEach
        public void setup() {
                orderRepository.deleteAll();
        }

        @Test
        @SneakyThrows
        @DisplayName("When Create an order then it is OK and return the order")
        void createOrderIsOk() {
                // Create a list of dummy orders for the mocked page
                List<OrderEntity> orders = OrderTestUtil.buildMultipleOrders(10);
                orderRepository.saveAllAndFlush(orders);

                var createOrderDTO = OrderDTO.builder().customerId(CUSTOMER_ID).productId(PRODUCT_ID)
                                .currency(EUR.toString())
                                .quantity(QUANTITY).price(PRICE).build();

                mockMvc.perform(post("/order").contentType(MediaType.APPLICATION_JSON)
                                .content(StreamBuilderTestUtils.convertObjectToJsonBytes(objectMapper, createOrderDTO)))
                                .andExpect(status().isOk())
                                .andDo(result -> await().atMost(Duration.ofSeconds(20))
                                                .untilAsserted(() -> assertEquals(11,
                                                                orderRepository.findAll().size())));
        }

        @Test
        @SneakyThrows
        @DisplayName("When Get an order then it is OK and return the order")
        void getOrderIsOK() {
                var expectedOrder = OrderCreatedEvent.newBuilder().setCustomerId(CUSTOMER_ID).setProductId(PRODUCT_ID)
                                .setCurrency(EUR.toString()).setStatus(OrderCreatedEvent.Status.NEW)
                                .setPrice(PRICE.doubleValue())
                                .setQuantity(QUANTITY.doubleValue()).build();

                var expectedOrderEntity = KafkaOrderMapper.toEntity(expectedOrder);

                OrderEntity save = orderRepository.save(expectedOrderEntity);

                mockMvc.perform(get("/order").param("order_id", save.getOrderId())).andExpect(status().isOk());
        }

        @Test
        @SneakyThrows
        @DisplayName("When Get an unknown order then it is not found")
        void getUnknownOrderIsNotFound() {
                var unknownOrderId = "UNKNOWN_ORDER_ID";

                mockMvc.perform(get("/order").param("order_id", unknownOrderId)).andExpect(status().isNotFound());
        }

        @Test
        @SneakyThrows
        @DisplayName("When Create an invalid order then it is bad request")
        void createInvalidOrderRequestIsBadRequest() {
                var createOrderDTO = OrderDTO.builder().productId(PRODUCT_ID).currency(EUR.toString())
                                .quantity(QUANTITY)
                                .price(PRICE).build();

                mockMvc.perform(post("/order").contentType(MediaType.APPLICATION_JSON)
                                .content(StreamBuilderTestUtils.convertObjectToJsonBytes(objectMapper, createOrderDTO)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @SneakyThrows
        @DisplayName("Get orders by product ids and statuses")
        void getOrderByProductIdsAndStatusesPageable() {
                // Create a list of dummy orders for the mocked page
                List<OrderEntity> orders = OrderTestUtil.buildMultipleOrders(10);
                orderRepository.saveAllAndFlush(orders);

                // Perform GET request for the first page
                for (int i = 0; i < 2; i++) {
                        mockMvc.perform(get("/order/{pageSize}/{offset}/{sortByFieldName}", 5, i, "status"))
                                        .andExpect(status().isOk()).andExpect(jsonPath("$.content").exists())
                                        .andExpect(jsonPath("$.content[*].status").exists())
                                        .andExpect(jsonPath("$.numberOfElements").value(5));
                }
        }

        @Test
        @SneakyThrows
        @DisplayName("When update an order then the order is updated successfully")
        void updateOrderThenOrderIsUpdated() {
                var order = OrderTestUtil.buildOrder(10);
                orderRepository.saveAndFlush(order);

                var updatedOrder = OrderDTO.builder()
                                .orderId(order.getOrderId())
                                .productId(order.getProductId())
                                .currency(order.getCurrency().getCurrencyCode())
                                .quantity(order.getQuantity())
                                .status(OrderStatus.CONFIRMED.toString())
                                .customerId(order.getCustomerId())
                                .price(order.getPrice())
                                .build();

                mockMvc.perform(put("/order").contentType(MediaType.APPLICATION_JSON)
                                .content(StreamBuilderTestUtils.convertObjectToJsonBytes(objectMapper, updatedOrder)))
                                .andExpect(status().isOk());

                orderRepository.findByOrderId(order.getOrderId()).ifPresent(oe -> assertAll(() -> {
                        assertThat(oe.getOrderId()).isEqualTo(order.getOrderId());
                        assertThat(oe.getProductId()).isEqualTo(order.getProductId());
                        assertThat(oe.getCurrency().getCurrencyCode()).isEqualTo(order.getCurrency().getCurrencyCode());
                        assertThat(oe.getQuantity().setScale(2, RoundingMode.HALF_UP))
                                        .isEqualTo(order.getQuantity().setScale(2, RoundingMode.HALF_UP));
                        assertThat(oe.getStatus()).isEqualTo(order.getStatus());
                        assertThat(oe.getCustomerId()).isEqualTo(order.getCustomerId());
                        assertThat(oe.getCreatedTime()).isNotNull();
                }));
        }

        @Test
        @SneakyThrows
        @DisplayName("When Get orders then it is OK and return the orders")
        void getOrderProductIdsAndStatusWithCriteria() {

                List<OrderEntity> orders = OrderTestUtil.buildMultipleOrders(10);
                orderRepository.saveAllAndFlush(orders);
                Set<String> productIds = new HashSet<>();
                productIds.add("product_1");
                productIds.add("product_2");

                Set<String> statuses = new HashSet<>();
                statuses.add(OrderStatus.NEW.toString());

                mockMvc.perform(get("/order/{pageSize}/{offset}/{sortByFieldName}", 5, 0, "productId")
                                .queryParam("productIds", String.join(",", productIds))).andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").exists()) // Verify the presence of "content" field in
                                                                           // the JSON
                                                                           // response
                                .andExpect(jsonPath("$.numberOfElements").value(2)); // Verify that the number of
                                                                                     // returned items is 2

                mockMvc.perform(get("/order/{pageSize}/{offset}/{sortByFieldName}", 5, 0, "productId")
                                .queryParam("statuses", String.join(",", statuses))).andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").exists()) // Verify the presence of "content" field in
                                                                           // the JSON
                                                                           // response
                                .andExpect(jsonPath("$.numberOfElements").value(5)); // Verify that the number of
                                                                                     // returned items is 3

                mockMvc.perform(get("/order/{pageSize}/{offset}/{sortByFieldName}", 5, 0, "productId")
                                .queryParam("productIds", String.join(",", productIds))
                                .queryParam("statuses", String.join(",", statuses)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").exists()) // Verify the presence of "content" field in
                                                                           // the JSON
                                                                           // response
                                .andExpect(jsonPath("$.numberOfElements").value(1)); // Verify that the number of
                                                                                     // returned items is 3
        }

}
