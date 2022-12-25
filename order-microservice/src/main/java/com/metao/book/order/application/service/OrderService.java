package com.metao.book.order.application.service;

import java.util.List;
import java.util.Optional;

import com.metao.book.shared.OrderEvent;
import org.springframework.stereotype.Service;

import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.domain.OrderServiceInterface;
import com.metao.book.order.infrastructure.OrderMapperInterface;
import com.metao.book.order.infrastructure.kafka.KafkaOrderProducer;
import com.metao.book.order.infrastructure.repository.KafkaOrderService;

import lombok.RequiredArgsConstructor;

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
                .peek(kafkaOrderProducer::produceOrderMessage)
                .map(OrderEvent::getOrderId)
                .findAny();
    }

    @Override
    public Optional<OrderDTO> getOrderByOrderId(String orderId) {
        return kafkaOrderService.getOrder(orderId);
    }

    @Override
    public Optional<List<OrderEvent>> getAllOrdersPageable(int from, int to) {
        return Optional.empty();
    }

}
