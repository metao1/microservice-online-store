package com.metao.book.order.infrastructure.kafka;

import com.google.protobuf.Timestamp;
import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderStatus;
import com.metao.book.order.domain.dto.OrderDTO;
import com.metao.book.shared.OrderCreatedEvent;
import com.metao.book.shared.OrderUpdatedEvent;
import com.metao.book.shared.domain.financial.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

public class KafkaOrderMapper {

    public static OrderCreatedEvent toOrderCreatedEvent(OrderDTO dto) {
        return OrderCreatedEvent.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setCreateTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
            .setStatus(getOrderStatus(dto.status()))
            .setCustomerId(dto.customerId())
            .setProductId(dto.productId())
            .setCurrency(dto.currency())
            .setQuantity(dto.quantity().doubleValue())
            .setPrice(dto.price().doubleValue())
            .build();
    }

    public static OrderUpdatedEvent toOrderUpdatedEvent(OrderDTO dto) {
        return OrderUpdatedEvent.newBuilder()
            .setId(dto.orderId())
            .setCustomerId(dto.customerId())
            .setProductId(dto.productId())
            .setCurrency(dto.currency())
            .setQuantity(dto.quantity().doubleValue())
            .setUpdateTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
            .setPrice(dto.price().doubleValue())
            .build();
    }

    public static OrderEntity toEntity(OrderUpdatedEvent oue) {
        var money = new Money(Currency.getInstance(oue.getCurrency()), BigDecimal.valueOf(oue.getPrice()));

        return new OrderEntity(
            oue.getCustomerId(),
            oue.getProductId(),
            BigDecimal.valueOf(oue.getQuantity()),
            money,
            getOrderStatus(oue.getStatus())
        );
    }

    public static OrderEntity toEntity(OrderCreatedEvent oce) {
        var money = new Money(Currency.getInstance(oce.getCurrency()), BigDecimal.valueOf(oce.getPrice()));

        return new OrderEntity(
            oce.getCustomerId(),
            oce.getProductId(),
            BigDecimal.valueOf(oce.getQuantity()),
            money,
            getOrderStatus(oce.getStatus())
        );
    }

    private static OrderStatus getOrderStatus(OrderCreatedEvent.Status status) {
        return switch (status) {
            case NEW -> OrderStatus.NEW;
            case SUBMITTED -> OrderStatus.SUBMITTED;
            case REJECTED -> OrderStatus.REJECTED;
            case CONFIRMED -> OrderStatus.CONFIRMED;
            case ROLLED_BACK -> OrderStatus.ROLLED_BACK;
            case UNRECOGNIZED -> null;
        };
    }

    private static OrderStatus getOrderStatus(OrderUpdatedEvent.Status status) {
        return switch (status) {
            case NEW -> OrderStatus.NEW;
            case SUBMITTED -> OrderStatus.SUBMITTED;
            case REJECTED -> OrderStatus.REJECTED;
            case CONFIRMED -> OrderStatus.CONFIRMED;
            case ROLLED_BACK -> OrderStatus.ROLLED_BACK;
            case UNRECOGNIZED -> null;
        };
    }

    private static OrderCreatedEvent.Status getOrderStatus(String status) {
        return switch (status) {
            case "NEW" -> OrderCreatedEvent.Status.NEW;
            case "SUBMITTED" -> OrderCreatedEvent.Status.SUBMITTED;
            case "REJECTED" -> OrderCreatedEvent.Status.REJECTED;
            case "CONFIRMED" -> OrderCreatedEvent.Status.CONFIRMED;
            case "ROLLED_BACK" -> OrderCreatedEvent.Status.ROLLED_BACK;
            default -> OrderCreatedEvent.Status.UNRECOGNIZED;
        };
    }
}
