package com.metao.book.checkout.application;

import static com.metao.book.shared.Status.ACCEPT;
import static com.metao.book.shared.Status.CONFIRM;
import static com.metao.book.shared.Status.REJECT;
import static com.metao.book.shared.Status.ROLLBACK;

import com.metao.book.checkout.domain.ProductInventoryEntity;
import com.metao.book.checkout.infrastructure.ProductInventoryRepository;
import com.metao.book.shared.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private static final String SOURCE = "payment";
    private final ProductInventoryRepository repository;
    private final KafkaTemplate<String, OrderEvent> template;

    @Value("${kafka.stream.topic.payment}")
    private String paymentOrdersTopic;

    public void reserve(OrderEvent order) {
        var productInventory = repository.findByAsin(order.getProductId()).orElseThrow();
        log.info("Found: {}", productInventory);
        if (order.getPrice() < productInventory.getAmountAvailable()) {
            order.setStatus(ACCEPT);
            productInventory.setAmountReserved(productInventory.getAmountReserved() + order.getPrice());
            productInventory.setAmountAvailable(productInventory.getAmountAvailable() - order.getPrice());
        } else {
            order.setStatus(REJECT);
        }
        order.setSource(SOURCE);
        repository.save(productInventory);
        template.send(paymentOrdersTopic, order.getOrderId(), order);
        log.info("Sent: {}", order);
    }

    public void confirm(OrderEvent order) {
        var productInventory = repository.findByAsin(order.getProductId()).orElseThrow();
        log.info("Found: {}", productInventory);
        if (order.getStatus().equals(CONFIRM)) {
            rollbackOrder(order, productInventory);
        } else if (order.getStatus().equals(ROLLBACK) && !order.getSource().equals(SOURCE)) {
            productInventory.setAmountAvailable(productInventory.getAmountAvailable() + order.getPrice());
            rollbackOrder(order, productInventory);
        }
    }

    private void rollbackOrder(OrderEvent order, ProductInventoryEntity customer) {
        customer.setAmountReserved(customer.getAmountReserved() - order.getPrice());
        repository.save(customer);
    }

}
