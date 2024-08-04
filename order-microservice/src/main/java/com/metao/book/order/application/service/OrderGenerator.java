package com.metao.book.order.application.service;

import com.metao.book.OrderCreatedEventOuterClass.OrderCreatedEvent;
import com.metao.book.OrderEventOuterClass.OrderEvent;
import com.metao.book.order.infrastructure.kafka.KafkaOrderProducer;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(havingValue = "dev", name = "spring.profiles.active")
public class OrderGenerator {

    private static final String CUSTOMER_ID = "CUSTOMER_ID";
    private final KafkaOrderProducer kafkaProducer;
    private final AtomicInteger atomicInteger = new AtomicInteger(1);
    private final Queue<String> products = new LinkedBlockingQueue<>();

    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    public void commandLineRunner() {
        var randomNumber = atomicInteger.getAndIncrement();
        var order = OrderCreatedEvent.newBuilder()
            .setStatus(OrderCreatedEvent.Status.NEW)
            .setId(OrderEvent.UUID.getDefaultInstance().toString())
            .setProductId(products.poll())
            .setAccountId(CUSTOMER_ID)
            .setQuantity(randomNumber)
            .setPrice(100)
            .setCurrency("USD")
            .build();
        kafkaProducer.sendToKafka(order);
    }
}
