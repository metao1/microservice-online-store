package com.metao.book.order.application.service;

import com.metao.book.order.application.dto.CreateOrderDTO;
import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.application.dto.exception.CreateOrderException;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.domain.Status;
import com.metao.book.order.infrastructure.kafka.KafkaConstants;
import com.metao.book.order.infrastructure.kafka.KafkaOrderProducer;
import com.metao.book.order.infrastructure.repository.OrderRepository;
import com.metao.book.order.infrastructure.repository.OrderSpecifications;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final KafkaOrderProducer kafkaOrderProducer;
    private final OrderRepository orderRepository;
    private final OrderMapper mapper;

    public Optional<String> createOrder(CreateOrderDTO orderDto) {
        final var orderEvent = Optional.of(orderDto)
            .map(mapper::toDto)
            .orElseThrow(CreateOrderException::new);
        kafkaOrderProducer.sendToKafka(orderEvent);
        return Optional.of(orderEvent.orderId());
    }

    public Optional<OrderDTO> getOrderByOrderId(OrderId orderId) {
        return orderRepository.findById(orderId).map(mapper::toDto);
    }

    public Page<OrderDTO> getOrderByProductIdsAndOrderStatus(
        Set<String> productIds, Set<Status> orderStatus, int offset, int pageSize, String sortByFieldName
    ) {
        var pageable = PageRequest.of(offset, pageSize, Sort.by(Sort.Direction.ASC, sortByFieldName));
        var spec = OrderSpecifications.findByOrdersByCriteria(productIds, orderStatus);
        return orderRepository.findAll(spec, pageable).map(mapper::toDto);
    }

}
