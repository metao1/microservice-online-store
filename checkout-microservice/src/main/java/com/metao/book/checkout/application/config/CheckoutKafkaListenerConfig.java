package com.metao.book.checkout.application.config;

import com.metao.book.checkout.application.model.CustomerNotFoundException;
import com.metao.book.checkout.application.service.OrderManagerService;
import com.metao.book.checkout.domain.CustomerEntity;
import com.metao.book.checkout.domain.CustomerRepository;
import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.Status;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Configuration
@EnableKafka
@RequiredArgsConstructor
@ImportAutoConfiguration(value = {KafkaSerdesConfig.class})
public class CheckoutKafkaListenerConfig {

    private final OrderManagerService orderManagerService;
    private final CustomerRepository customerRepository;

    @Transactional(value = "kafkaTransactionManager")
    @KafkaListener(id = "checkout-order-id", topics = "${kafka.topic.order}")
    public void orderKafkaListener(ConsumerRecord<String, OrderEvent> record) {
        var order = record.value();
        log.info("Consumed order -> {}", order);
        var customer = customerRepository.findCustomerEntityByName(order.getCustomerId())
            .orElseThrow(() -> new CustomerNotFoundException());
        if (Status.NEW.equals(order.getStatus())) {
            orderManagerService.reserve(order, customer);
        } else {
            orderManagerService.confirm(order, customer);
        }
        customerRepository.save(customer);
    }

    @PostConstruct
    public void afterConstruct() {
        log.info("saving customer for test");
        var customer = new CustomerEntity();
        customer.setName("customer id");
        customer.setAmountReserved(BigDecimal.ZERO);
        customer.setAmountAvailable(BigDecimal.valueOf(100d));
        customerRepository.save(customer);
    }
}
