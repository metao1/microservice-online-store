package com.metao.book.order.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.Timestamp;
import com.metao.book.order.BaseKafkaIT;
import com.metao.book.order.OrderPaymentEvent;
import com.metao.book.order.OrderTestUtil;
import com.metao.book.order.application.card.OrderRepository;
import com.metao.book.order.domain.OrderEntity;
import java.time.Instant;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

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
        OrderEntity orderEntity = OrderTestUtil.buildOrder(1);

        orderRepository.save(orderEntity);
        var orderPaymentEvent = OrderPaymentEvent.newBuilder().setId(orderEntity.id().toUUID())
            .setCreateTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
            .setProductId(orderEntity.getProductId()).setCustomerId(orderEntity.getCustomerId())
            .setStatus(OrderPaymentEvent.Status.CONFIRMED).build();

        kafkaTemplate.send(orderPaymentTopic, orderEntity.getCustomerId(), orderPaymentEvent)
            .thenRun(() -> assertThat(orderRepository.findByOrderId(orderEntity.id().toUUID())).isPresent());
    }

}
