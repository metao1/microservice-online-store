package com.metao.book.order.infrastructure;

import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.domain.OrderEntity;
import com.metao.book.shared.Currency;
import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.Status;
import java.time.Instant;

public interface OrderMapperInterface {

    default OrderEvent toAvro(OrderDTO dto) {
        return OrderEvent.newBuilder()
            .setStatus(Status.NEW)
            .setCurrency(Currency.dlr)
            .setPrice(dto.getPrice().doubleValue())
            .setSource("STOCK")
            .setCreatedOn(Instant.now().toEpochMilli())
            .setStatus(Status.valueOf(dto.getStatus().toString()))
            .setQuantity(dto.getQuantity().doubleValue())
            .setProductId(dto.getProductId())
            .setCustomerId(dto.getCustomerId())
            .setOrderId(dto.getOrderId())
            .build();
    }

    static com.metao.book.order.domain.Status convertStatus(com.metao.book.shared.Status status) {
        return switch (status) {
            case NEW -> com.metao.book.order.domain.Status.NEW;
            case ACCEPT -> com.metao.book.order.domain.Status.ACCEPT;
            case CONFIRM -> com.metao.book.order.domain.Status.CONFIRM;
            case REJECT -> com.metao.book.order.domain.Status.REJECT;
            case PAYMENT -> com.metao.book.order.domain.Status.PAYMENT;
            case PRODUCT -> com.metao.book.order.domain.Status.PRODUCT;
            case ROLLBACK -> com.metao.book.order.domain.Status.ROLLBACK;
        };
    }

    static com.metao.book.shared.domain.financial.Currency convertToCurrency(Currency currency) {
        return com.metao.book.shared.domain.financial.Currency.valueOf(currency.name().toUpperCase());
    }

    static OrderDTO toOrderDTO(OrderEntity orderEntity) {
        return OrderDTO.builder()
            .orderId(orderEntity.id().toUUID())
            .price(orderEntity.getPrice())
            .currency(orderEntity.getCurrency())
            .productId(orderEntity.getProductId())
            .status(orderEntity.getStatus())
            .customerId(orderEntity.getCustomerId())
            .quantity(orderEntity.getProductCount())
            .build();
    }
}
