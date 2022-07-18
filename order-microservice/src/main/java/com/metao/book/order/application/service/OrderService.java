package com.metao.book.order.application.service;

import com.metao.book.order.application.dto.ProductDTO;
import com.metao.book.order.domain.OrderServiceInterface;
import com.metao.book.order.infrastructure.kafka.KafkaOrderProducer;
import com.metao.book.order.infrastructure.repository.KafkaOrderService;
import com.order.microservice.avro.Currency;
import com.order.microservice.avro.OrderAvro;
import com.order.microservice.avro.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService implements OrderServiceInterface {

    private final KafkaTemplate<Long, OrderAvro> kafkaTemplate;
    private final KafkaOrderService kafkaOrderService;
    private final KafkaOrderProducer kafkaProducer;
    private final FileHandler fileHandler;
    private final ProductDtoMapper mapper;
    AtomicInteger atomicInteger = new AtomicInteger(1);
    @Value("${kafka.stream.topic.order}")
    private String topic;
    @Value("${kafka.stream.topic.order}")
    private String orderTopic;
    private List<String> productAsinList = new ArrayList<>();
    private Random random = new Random();

    @Override
    public void saveOrder(OrderAvro orderAvro) {
        kafkaTemplate.send(orderTopic, orderAvro.getOrderId(), orderAvro);
    }

    @Override
    public Optional<OrderAvro> getOrderByProductId(String productId) {
        return kafkaOrderService.getOrder(productId);
    }

    @Scheduled(fixedDelay = 10000, initialDelay = 2000)
    public void commandLineRunner() {
        Optional.of(atomicInteger.getAndIncrement())
                .map(s -> OrderAvro
                        .newBuilder()
                        .setOrderId(s)
                        .setProductId(productAsinList.get(random.nextInt(productAsinList.size())))
                        .setCustomerId(s)
                        .setStatus(Status.NEW)
                        .setQuantity(1)
                        .setPrice(100)
                        .setCurrency(Currency.dlr)
                        .setSource("PAYMENT")
                        .build())
                .ifPresent(order -> kafkaProducer.send(topic, order.getOrderId(), order));
    }

    public void loadProducts() {
        log.info("importing products data from resources");
        try (var source = fileHandler.readFromFile("data/products.txt")) {
            this.productAsinList = source
                    .map(mapper::convertToDto)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(ProductDTO::getAsin)
                    .toList();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        log.info("finished writing to database.");
    }

    @Override
    public Optional<List<OrderAvro>> getAllOrdersPageable(int from, int to) {
        return Optional.empty();
    }

    @PostConstruct
    public void afterPropertiesSet() {
        loadProducts();
    }
}
