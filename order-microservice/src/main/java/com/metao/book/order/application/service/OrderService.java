package com.metao.book.order.application.service;

import com.metao.book.order.domain.OrderServiceInterface;
import com.metao.book.order.infrastructure.kafka.KafkaOrderProducer;
import com.metao.book.order.infrastructure.repository.KafkaOrderService;
import com.order.microservice.avro.Currency;
import com.order.microservice.avro.OrderAvro;
import com.order.microservice.avro.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class OrderService implements OrderServiceInterface {

    private final KafkaTemplate<Long, OrderAvro> kafkaTemplate;
    private final KafkaOrderService kafkaOrderService;
    private final KafkaOrderProducer kafkaProducer;

    @Value("${kafka.stream.topic.order}")
    private String orderTopic;

    @Override
    public void saveOrder(OrderAvro orderAvro) {
        kafkaTemplate.send(orderTopic, orderAvro.getOrderId(), orderAvro);
    }

    @Override
    public Optional<OrderAvro> getOrderByProductId(String productId) {
        return kafkaOrderService.getOrder(productId);
    }

    @Value("${kafka.stream.topic.order}")
    private String topic;

    AtomicInteger atomicInteger = new AtomicInteger(1);

    @Scheduled(fixedDelay = 10000, initialDelay = 2000)
    public void commandLineRunner() {
        Optional.of(atomicInteger.getAndIncrement())
                .map(s -> OrderAvro
                        .newBuilder()
                        .setOrderId(s)
                        .setProductId(s)
                        .setCustomerId(s)
                        .setStatus(Status.NEW)
                        .setQuantity(1)
                        .setPrice(100)
                        .setCurrency(Currency.dlr)
                        .setSource("PAYMENT")
                        .build())
                .ifPresent(order -> kafkaProducer.send(topic, order.getOrderId(), order));
    }

    @Override
    public Optional<List<OrderAvro>> getAllOrdersPageable(int from, int to) {
        return Optional.empty();
    }

}
