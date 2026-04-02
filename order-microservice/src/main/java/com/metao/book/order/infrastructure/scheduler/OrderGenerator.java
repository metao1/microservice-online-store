package com.metao.book.order.infrastructure.scheduler;

import com.google.protobuf.Message;
import com.metao.book.shared.OrderCreatedEvent;
import com.metao.book.shared.OrderCreatedEvent.OrderItem;
import com.metao.kafka.KafkaEventHandler;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(havingValue = "dev", name = "spring.profiles.active")
public class OrderGenerator {

    private static final String ACCOUNT_ID = "ACCOUNT_ID";
    private final KafkaEventHandler eventHandler;
    private final KafkaTemplate<String, Message> kafkaTemplate;
    private final AtomicInteger atomicInteger = new AtomicInteger(1);
    private final Queue<String> products = new LinkedBlockingQueue<>();

    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    public void commandLineRunner() {
        var randomNumber = atomicInteger.getAndIncrement();
        var orderEvent = OrderCreatedEvent.newBuilder().setStatus(OrderCreatedEvent.Status.CREATED)
            .setId(OrderCreatedEvent.UUID.getDefaultInstance().toString())
            .setUserId(ACCOUNT_ID)
            .addItems(OrderItem.newBuilder()
                .setSku(products.poll())
                .setCurrency("EUR")
                .setPrice(randomNumber)
                .setProductTitle(randomNumber + "")
                .build()
            )
            .build();

        var topic = eventHandler.getKafkaTopic(orderEvent.getClass());
        kafkaTemplate.send(topic, orderEvent.getUserId(), orderEvent);
    }

}
