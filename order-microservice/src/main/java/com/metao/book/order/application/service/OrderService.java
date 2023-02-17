package com.metao.book.order.application.service;

import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.domain.OrderServiceInterface;
import com.metao.book.order.domain.Status;
import com.metao.book.order.infrastructure.OrderMapperInterface;
import com.metao.book.order.infrastructure.kafka.KafkaOrderProducer;
import com.metao.book.order.infrastructure.repository.KafkaOrderService;
import com.metao.book.shared.OrderEvent;
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
        return Optional.of(orderDto)
            .map(mapper::toAvro)
            .stream()
            .<OrderEvent>mapMulti((order, consumer) -> {
                if (order != null) {
                    consumer.accept(order);
                }
            })
            .peek(kafkaOrderProducer::handle)
            .map(OrderEvent::getOrderId)
            .findAny();
    }

    @Override
    public Optional<OrderDTO> getOrderByOrderId(OrderId orderId) {
        return kafkaOrderService.getOrder(orderId);
    }

    @Override
    public Optional<List<OrderDTO>> getAllOrdersPageable(OrderId from, OrderId to, Set<Status> statusSet) {
        return kafkaOrderService.getOrders(from, to, statusSet);
    }

}
