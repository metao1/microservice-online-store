package com.metao.book.order.infrastructure;

import com.google.protobuf.Timestamp;
import com.metao.book.order.BaseKafkaIT;
import com.metao.book.order.OrderPaymentEvent;
import com.metao.book.order.OrderTestUtil;
import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.infrastructure.repository.OrderRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderCalculationTest extends BaseKafkaIT {

    @Autowired
    KafkaTemplate<String, OrderPaymentEvent> kafkaTemplate;

    @Autowired
    OrderRepository orderRepository;

    @Value("${kafka.topic.order-payment.name}")
    String orderPaymentTopic;

    @Test
    @SneakyThrows
    @DisplayName("Send order payment event")
    void sendOrderPaymentEventTest() {
        OrderEntity orderEntity = OrderTestUtil.buildOrderEntity(1);

        /*Mockito.when(orderRepository.findByOrderId(any()))
                .thenReturn(Optional.of(orderEntity));*/

        orderRepository.save(orderEntity);
        var orderPaymentEvent = OrderPaymentEvent.newBuilder()
                .setId(orderEntity.id().toUUID())
                .setCreateTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
                .setProductId(orderEntity.getProductId())
                .setCustomerId(orderEntity.getCustomerId())
                .setStatus(OrderPaymentEvent.Status.CONFIRMED)
                .build();

        kafkaTemplate.send(orderPaymentTopic, orderEntity.getCustomerId(), orderPaymentEvent)
                .thenRun(() -> assertTrue(orderRepository.findById(orderEntity.id()).isPresent()));
    }

}
