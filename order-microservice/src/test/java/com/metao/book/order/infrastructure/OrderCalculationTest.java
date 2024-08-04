package com.metao.book.order.infrastructure;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.metao.book.order.BaseKafkaIT;
import com.metao.book.order.OrderPaymentEvent;
import com.metao.book.order.OrderPaymentEvent.Status;
import com.metao.book.order.OrderTestUtil;
import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.infrastructure.repository.OrderRepository;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class OrderCalculationTest extends BaseKafkaIT {

    @Autowired
    KafkaTemplate<String, OrderPaymentEvent> kafkaTemplate;

    @MockBean
    OrderRepository orderRepository;

    @Value("${kafka.topic.order-payment.name}")
    String topic;

    @BeforeAll
    public void setup() {
        super.setup();
    }

    @Test
    @SneakyThrows
    void sendOrderPaymentEventTest() {
        OrderEntity orderEntity = OrderTestUtil.buildOrderEntity(1);

        Mockito.when(orderRepository.findByOrderId(any(OrderId.class)))
            .thenReturn(Optional.of(orderEntity));

        var orderPaymentEvent = OrderPaymentEvent.newBuilder()
            .setId(orderEntity.id().toUUID())
            .setProductId(orderEntity.getProductId())
            .setCustomerId(orderEntity.getCustomerId())
            .setCurrency(orderEntity.getCurrency().toString())
            .setQuantity(orderEntity.getQuantity().doubleValue())
            .setPrice(orderEntity.getPrice().doubleValue())
            .setStatus(Status.CONFIRMED)
            .build();

        kafkaTemplate.send(topic, orderEntity.getCustomerId(), orderPaymentEvent)
            .thenRun(() -> log.debug("Order {} sent", orderPaymentEvent));

        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> verify(orderRepository).save(any(OrderEntity.class)));
    }

}
