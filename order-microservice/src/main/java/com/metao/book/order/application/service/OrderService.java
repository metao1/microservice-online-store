package com.metao.book.order.application.service;

import com.metao.book.order.application.dto.CreateOrderDTO;
import com.metao.book.order.application.dto.exception.CouldNotCreateOrderException;
import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.domain.OrderServiceInterface;
import com.metao.book.order.domain.Status;
import com.metao.book.order.infrastructure.OrderMapperInterface;
import com.metao.book.order.infrastructure.kafka.KafkaConstants;
import com.metao.book.order.infrastructure.kafka.KafkaOrderProducer;
import com.metao.book.order.infrastructure.repository.KafkaOrderService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(transactionManager = KafkaConstants.TRANSACTION_MANAGER, propagation = Propagation.REQUIRED)
public class OrderService implements OrderServiceInterface {

    private final KafkaOrderService kafkaOrderService;
    private final KafkaOrderProducer kafkaOrderProducer;
    private final OrderMapperInterface mapper;

    @Override
    public Optional<String> createOrder(CreateOrderDTO orderDto) {
        final var orderEvent = Optional.of(orderDto)
            .map(mapper::toEvent)
            .orElseThrow(CouldNotCreateOrderException::new);
        kafkaOrderProducer.sendToKafka(orderEvent);
        return Optional.of(orderEvent.orderId());
    }

    @Override
    public Optional<OrderEntity> getOrderByOrderId(OrderId orderId) {
        return kafkaOrderService.getOrder(orderId);
    }

    @Override
    public Optional<List<OrderEntity>> getOrderByProductIdsAndOrderStatus(
            Set<String> productIds,
            Set<Status> orderStatus) {
        return kafkaOrderService.searchOrdersWithProductId(productIds, orderStatus);
    }

    @Override
    public Optional<List<OrderEntity>> getAllOrdersPageable(OrderId from, OrderId to, Set<Status> statusSet) {
        return kafkaOrderService.getOrders(from, to, statusSet);
    }

}
