package com.metao.book.order.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.order.application.dto.CreateOrderDTO;
import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.Status;
import com.metao.book.shared.domain.financial.Money;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderMapper {

    private final ObjectMapper objectMapper;

    public OrderDTO toDto(String orderVal) {
        final OrderDTO orderDTO;
        try {
            orderDTO = objectMapper.readValue(orderVal, OrderDTO.class);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return orderDTO;
    }

    public OrderDTO toDto(CreateOrderDTO dto) {
        return OrderDTO.builder()
            .orderId(UUID.randomUUID().toString())
            .customerId(dto.accountId())
            .productId(dto.productId())
            .currency(dto.currency())
            .quantity(dto.quantity())
            .status(Status.NEW)
            .price(dto.price())
            .build();
    }

    public OrderDTO toDto(OrderEntity orderEntity) {
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

    public OrderEntity toEntity(OrderDTO order) {
        var money = new Money(order.currency(), order.price());
        return new OrderEntity(order.orderId(), order.customerId(), order.productId(), order.quantity(), money,
            order.status());
    }
}
