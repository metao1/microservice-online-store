package com.metao.book.order.application.service;

import com.google.protobuf.Timestamp;
import com.metao.book.order.OrderCreatedEvent;
import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderStatus;
import com.metao.book.shared.domain.financial.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

import static com.metao.book.order.OrderCreatedEvent.Status;
import static com.metao.book.order.OrderCreatedEvent.Status.SUBMITTED;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMapper {

    public OrderCreatedEvent toOrderCreatedEvent(OrderDTO dto) {
        return OrderCreatedEvent.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setCreateTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
                .setStatus(Status.NEW)
                .setCustomerId(dto.customerId())
                .setProductId(dto.productId())
                .setCurrency(dto.currency())
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
                .setStatus(switch (orderEntity.getStatus()) {
                    case SUBMITTED -> SUBMITTED;
                    case REJECTED -> Status.REJECTED;
                    case CONFIRMED -> Status.CONFIRMED;
                    case NEW -> Status.NEW;
                    case ROLLED_BACK -> Status.ROLLED_BACK;
                })
                .build();
    }

    public OrderEntity toEntity(OrderCreatedEvent oce) {
        var money = new Money(Currency.getInstance(oce.getCurrency()), BigDecimal.valueOf(oce.getPrice()));

        var status = switch (oce.getStatus()) {
            case SUBMITTED -> OrderStatus.SUBMITTED;
            case REJECTED -> OrderStatus.REJECTED;
            case NEW -> OrderStatus.NEW;
            case CONFIRMED -> OrderStatus.CONFIRMED;
            case ROLLED_BACK -> OrderStatus.ROLLED_BACK;
            case UNRECOGNIZED -> null;
        };

        return new OrderEntity(
                oce.getCustomerId(),
                oce.getProductId(),
                BigDecimal.valueOf(oce.getQuantity()),
                money,
                status
        );
    }

    public OrderDTO toOrderDTO(OrderEntity orderEntity) {
        return OrderDTO.builder()
                .orderId(orderEntity.getOrderId())
                .customerId(orderEntity.getCustomerId())
                .productId(orderEntity.getProductId())
                .currency(orderEntity.getCurrency().toString())
                .status(orderEntity.getStatus().toString())
                .quantity(orderEntity.getQuantity())
                .price(orderEntity.getPrice())
                .build();
    }

}
