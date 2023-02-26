package com.metao.book.order.application.service;

import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.application.dto.exception.CouldNotCreateOrderException;
import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.domain.OrderServiceInterface;
import com.metao.book.order.domain.Status;
import com.metao.book.order.infrastructure.OrderMapperInterface;
import com.metao.book.order.infrastructure.kafka.KafkaOrderProducer;
import com.metao.book.order.infrastructure.repository.KafkaOrderService;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService implements OrderServiceInterface {

    private final KafkaOrderService kafkaOrderService;
    private final KafkaOrderProducer kafkaOrderProducer;
    private final OrderMapperInterface mapper;

    @Override
    public Optional<String> createOrder(OrderDTO orderDto) {
        final var orderEvent = Optional.of(orderDto)
            .map(mapper::toAvro)
            .orElseThrow(CouldNotCreateOrderException::new);
        try {
            kafkaOrderProducer.sendToKafka(orderEvent);
            return Optional.of("ok");
        } catch (Exception e) {
            throw new CouldNotCreateOrderException(e);
        }
    }

    @Override
    public Optional<OrderEntity> getOrderByOrderId(OrderId orderId) {
        return kafkaOrderService.getOrder(orderId);
    }

    @Override
    public Optional<List<OrderEntity>> getOrderByProductIdsAndOrderStatus(
        Set<String> productIds,
        Set<Status> orderStatus
    ) {
        return kafkaOrderService.searchOrdersWithProductId(productIds, orderStatus);
    }

    @Override
    public Optional<List<OrderEntity>> getAllOrdersPageable(OrderId from, OrderId to, Set<Status> statusSet) {
        return kafkaOrderService.getOrders(from, to, statusSet);
    }

}
