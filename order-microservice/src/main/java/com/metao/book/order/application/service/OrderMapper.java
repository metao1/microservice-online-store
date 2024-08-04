package com.metao.book.order.application.service;

import com.metao.book.OrderCreatedEventOuterClass.OrderCreatedEvent;
import com.metao.book.OrderCreatedEventOuterClass.OrderCreatedEvent.Status;
import com.metao.book.order.application.dto.CreateOrderDTO;
import com.metao.book.order.domain.OrderEntity;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class OrderMapper {

    public OrderCreatedEvent toOrderCreatedEvent(CreateOrderDTO dto) {
        return OrderCreatedEvent.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setStatus(Status.NEW)
            .setCustomerId(dto.accountId())
            .setProductId(dto.productId())
            .setCurrency(dto.currency().toString())
            .setQuantity(dto.quantity().doubleValue())
            .setPrice(dto.price().doubleValue())
            .build();
    }

    public OrderCreatedEvent toOrderCreatedEvent(OrderEntity orderEntity) {
        return OrderCreatedEvent.newBuilder()
            .setId(orderEntity.id().toUUID())
            .setPrice(orderEntity.getPrice().doubleValue())
            .setCurrency(orderEntity.getCurrency().toString())
            .setProductId(orderEntity.getProductId())
            .setCustomerId(orderEntity.getCustomerId())
            .setQuantity(orderEntity.getQuantity().doubleValue())
            .setStatus(
                switch (orderEntity.getStatus()) {
                    case SUBMITTED -> Status.SUBMITTED;
                    case REJECTED -> Status.REJECTED;
                    case CONFIRMED -> Status.CONFIRMED;
                    case NEW -> Status.NEW;
                    case ROLLED_BACK -> Status.ROLLED_BACK;
                })
            .build();
    }

    public abstract <T> OrderEntity toEntity(T order);
}
