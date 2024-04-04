package com.metao.book.order.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.order.application.dto.CreateOrderDTO;
import com.metao.book.order.application.dto.OrderCreatedEvent;
import com.metao.book.order.domain.OrderEntity;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.order.OrderStatus;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderMapper {

    private final ObjectMapper objectMapper;

    public OrderCreatedEvent toOrderCreatedEvent(String orderVal) {
        final OrderCreatedEvent orderCreatedEvent;
        try {
            orderCreatedEvent = objectMapper.readValue(orderVal, OrderCreatedEvent.class);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return orderCreatedEvent;
    }

    public OrderCreatedEvent toOrderCreatedEvent(CreateOrderDTO dto) {
        return OrderCreatedEvent.builder()
            .orderId(UUID.randomUUID().toString())
            .status(OrderStatus.NEW)
            .customerId(dto.accountId())
            .productId(dto.productId())
            .currency(dto.currency())
            .quantity(dto.quantity())
            .price(dto.price())
            .build();
    }

    public OrderCreatedEvent toOrderCreatedEvent(OrderEntity orderEntity) {
        return OrderCreatedEvent.builder()
            .orderId(orderEntity.id().toUUID())
            .price(orderEntity.getPrice())
            .currency(orderEntity.getCurrency())
            .productId(orderEntity.getProductId())
            .customerId(orderEntity.getCustomerId())
            .quantity(orderEntity.getProductCount())
            .status(orderEntity.getStatus())
            .build();
    }

    public OrderEntity toEntity(OrderCreatedEvent order) {
        var money = new Money(order.currency(), order.price());
        return new OrderEntity(order.orderId(), order.customerId(), order.productId(), order.quantity(), money,
            order.status());
    }
}
