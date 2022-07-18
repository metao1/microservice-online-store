package com.metao.book.retails.application.service;

import com.metao.book.retails.application.exception.ProductNotFoundException;
import com.metao.book.retails.domain.ProductEntity;
import com.metao.book.retails.domain.ProductId;
import com.metao.book.retails.domain.ProductRepository;
import com.order.microservice.avro.OrderAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.order.microservice.avro.Status.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderManageService {

    private static final String SOURCE = "payment";
    private final ProductRepository productRepository;
    private final KafkaOrderProducer kafkaOrderProducer;

    @Value("${kafka.stream.topic.payment-order}")
    private String paymentOrderTopic;

    public void reserve(OrderAvro order) {
        final ProductEntity product;
        try {
            product = productRepository.findById(new ProductId(order.getProductId())).orElseThrow(() -> new ProductNotFoundException(""));
            log.info("Found: {}", product);
            //if (order.getPrice() < product.getPriceValue()) {
            order.setStatus(ACCEPT);
            product.setReservedItems(product.getReservedItems() + order.getQuantity());
            product.setAvailableItems(product.getAvailableItems() - order.getQuantity());
//            } else {
//                order.setStatus(REJECT);
//            }
            order.setSource(SOURCE);
            productRepository.save(product);
            kafkaOrderProducer.send(paymentOrderTopic, order.getOrderId(), order);
            log.info("Sent: {}", order);
        } catch (Exception e) {

        }
    }

    public void confirm(OrderAvro order) {
        var product = productRepository.findById(new ProductId(order.getProductId() + "")).orElseThrow();
        log.info("Found: {}", product);
        if (order.getStatus().equals(CONFIRM)) {
            product.setReservedItems(product.getReservedItems() - order.getPrice());
            productRepository.save(product);
        } else if (order.getStatus().equals(ROLLBACK) && !order.getSource().equals(SOURCE)) {
            product.setReservedItems(product.getReservedItems() - order.getPrice());
            product.setAvailableItems(product.getAvailableItems() + order.getPrice());
            productRepository.save(product);
        }
    }
}