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
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class OrderService implements OrderServiceInterface {

    private final KafkaTemplate<String, OrderAvro> kafkaTemplate;
    private final KafkaOrderService kafkaOrderService;

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

    @Autowired
    private KafkaOrderProducer kafkaProducer;


    @Value("${kafka.stream.topic.order}")
    private String topic;

    @Scheduled(fixedDelay = 10000, initialDelay = 2000)
    public void commandLineRunner() {
        IntStream.range(0, 10)
                .boxed()
                .map(String::valueOf)
                .map(s -> OrderAvro
                        .newBuilder()
                        .setOrderId("order-" + s)
                        .setProductId("product - " + s)
                        .setCustomerId("customer - " + s)
                        .setStatus(Status.NEW)
                        .setQuantity(1)
                        .setPrice(100.0)
                        .setCurrency(Currency.dlr)
                        .build())
                .forEach(orderDTO -> kafkaProducer.send(topic, orderDTO.getOrderId(), orderDTO));
    }

    @Override
    public Optional<List<OrderAvro>> getAllOrdersPageble(int from, int to) {
        return Optional.empty();
    }

}
