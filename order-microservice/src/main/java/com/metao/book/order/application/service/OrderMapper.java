package com.metao.book.order.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.infrastructure.OrderMapperInterface;
import com.metao.book.shared.domain.financial.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderMapper implements OrderMapperInterface {
    private final ObjectMapper objectMapper;

    public OrderEntity toEntity(OrderDTO order) {
        var money = new Money(order.currency(), order.price());
        return new OrderEntity(order.orderId(), order.customerId(), order.productId(), order.quantity(), money,
            order.status());
    }

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
}
