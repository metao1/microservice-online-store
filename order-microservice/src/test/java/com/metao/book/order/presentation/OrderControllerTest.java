package com.metao.book.order.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.OrderEventOuterClass.OrderEvent;
import com.metao.book.order.application.dto.CreateOrderDTO;
import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.domain.Status;
import com.metao.book.shared.domain.financial.Currency;
import com.metao.book.shared.test.StreamBuilderTestUtils;
import java.math.BigDecimal;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerTest extends BaseKafkaIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc restTemplate;

    @Value("${kafka.topic.order}")
    String orderTopic;

    ArrayBlockingQueue<OrderDTO> arrayBlockingQueue = new ArrayBlockingQueue<>(1);
    @Autowired
    KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Test
    @SneakyThrows
    void createOrderIsOk() {
        var createOrderDTO = CreateOrderDTO.builder()
            .accountId("ACCOUNT_ID")
                .productId("1234567892")
                .currency(Currency.EUR)
                .quantity(BigDecimal.valueOf(100d))
                .price(BigDecimal.valueOf(123d))
                .build();

        var expectedOrder = OrderDTO.builder()
            .customerId("ACCOUNT_ID")
            .productId("1234567892")
            .currency(Currency.EUR)
            .status(Status.NEW)
            .price(BigDecimal.valueOf(123d))
            .quantity(BigDecimal.valueOf(100d))
            .build();

        restTemplate.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(StreamBuilderTestUtils.convertObjectToJsonBytes(objectMapper, createOrderDTO)))
                .andExpect(status().isOk());

        OrderDTO orderDTO = arrayBlockingQueue.poll(10, TimeUnit.SECONDS);

        assertThat(orderDTO)
            .isNotNull()
            .satisfies(order -> {
                assertEquals(expectedOrder.price(), order.price());
                assertEquals(expectedOrder.productId(), order.productId());
                assertEquals(expectedOrder.quantity(), order.quantity());
                assertEquals(expectedOrder.status(), order.status());
                assertEquals(expectedOrder.currency(), order.currency());
                assertEquals(expectedOrder.customerId(), order.customerId());
            });

    }

    @SneakyThrows
    @KafkaListener(id = "order-listener-test", topics = "${kafka.topic.order}", groupId = "order-grp")
    @Transactional
    public void onOrderListenerTest(
        ConsumerRecord<String, String> record
    ) {
        final OrderDTO orderDTO;
        try {
            orderDTO = objectMapper.readValue(record.value(), OrderDTO.class);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
        arrayBlockingQueue.put(orderDTO);
    }

}
