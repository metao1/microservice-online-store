package com.metao.book.order.application.service;

import com.metao.book.OrderCreatedEventOuterClass.OrderCreatedEvent;
import com.metao.book.order.application.dto.CreateOrderDTO;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.infrastructure.kafka.KafkaOrderProducer;
import com.metao.book.order.infrastructure.repository.OrderRepository;
import com.metao.book.order.infrastructure.repository.OrderSpecifications;
import com.metao.book.shared.application.service.StageProcessor;
import com.metao.book.shared.domain.order.OrderStatus;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final KafkaOrderProducer kafkaOrderProducer;
    private final OrderRepository orderRepository;
    private final OrderMapperFactory mapper;

    public String createOrder(CreateOrderDTO orderDto) {
        return StageProcessor.accept(orderDto)
            .map(mapper::toOrderCreatedEvent)
            .applyExceptionally((orderCreatedEvent, exp) -> {
                    kafkaOrderProducer.sendToKafka(orderCreatedEvent);
                return orderCreatedEvent.getId();
                });
    }

    public Optional<OrderCreatedEvent> getOrderByOrderId(OrderId orderId) {
        return orderRepository.findById(orderId).map(mapper::toOrderCreatedEvent);
    }

    public Page<OrderCreatedEvent> getOrderByProductIdsAndOrderStatus(
            Set<String> productIds, Set<OrderStatus> statuses, int offset, int pageSize, String sortByFieldName
    ) {
        var pageable = PageRequest.of(offset, pageSize, Sort.by(Sort.Direction.ASC, sortByFieldName));
        var spec = OrderSpecifications.findByOrdersByCriteria(productIds, statuses);
        return orderRepository.findAll(spec, pageable).map(mapper::toOrderCreatedEvent);
    }

}
