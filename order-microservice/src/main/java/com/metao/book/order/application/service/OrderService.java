package com.metao.book.order.application.service;

import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.domain.OrderStatus;
import com.metao.book.order.infrastructure.kafka.KafkaOrderProducer;
import com.metao.book.order.infrastructure.repository.OrderRepository;
import com.metao.book.order.infrastructure.repository.OrderSpecifications;
import com.metao.book.shared.application.service.StageProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final KafkaOrderProducer kafkaOrderProducer;
    private final OrderRepository orderRepository;
    private final OrderMapper mapper;

    public String createOrder(OrderDTO orderDto) {
        return StageProcessor.accept(orderDto)
            .map(mapper::toOrderCreatedEvent)
            .applyExceptionally((orderCreatedEvent, exp) -> {
                    kafkaOrderProducer.sendToKafka(orderCreatedEvent);
                return orderCreatedEvent.getId();
                });
    }

    public Optional<OrderDTO> getOrderByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId).map(mapper::toOrderDTO);
    }

    public Page<OrderDTO> getOrderByProductIdsAndOrderStatus(
            Set<String> productIds, Set<OrderStatus> statuses, int offset, int pageSize, String sortByFieldName
    ) {
        var pageable = PageRequest.of(offset, pageSize, Sort.by(Sort.Direction.ASC, sortByFieldName));
        var spec = OrderSpecifications.findByOrdersByCriteria(productIds, statuses);
        return orderRepository.findAll(spec, pageable).map(mapper::toOrderDTO);
    }

}
