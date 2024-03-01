package com.metao.book.order.application.service;

import com.metao.book.order.infrastructure.kafka.KafkaOrderProducer;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(havingValue = "dev", name = "spring.profiles.active")
public class OrderGenerator {

    private static final String CUSTOMER_ID = "CUSTOMER_ID";
    private final Random random = new Random();
    private final KafkaOrderProducer kafkaProducer;
    private final AtomicInteger atomicInteger = new AtomicInteger(1);
    @Value("${kafka.topic.order}")
    String orderTopic;
    private final Queue<String> products = new LinkedBlockingQueue<>();

    //@Scheduled(fixedDelay = 30000, initialDelay = 10000)
//    public void commandLineRunner() {
//        var randomNumber = atomicInteger.getAndIncrement();
//        var order = OrderEvent.Body.newBuilder()
//            .setId(OrderEvent.UUID.getDefaultInstance())
//            .setProductId(products.get(random.nextInt(products.size())))
//            .setAccountId(CUSTOMER_ID)
//            .setStatus(status.NEW)
//            .setQuantity(1)
//            .setPrice(100)
//            .setCurrency("dlr")
//            .build();
//        kafkaProducer.sendToKafka(order);
//    }
}
