package com.metao.book.order.domain;

import com.metao.book.order.application.card.OrderRepository;
import com.metao.book.order.application.card.OrderSpecifications;
import com.metao.book.order.infrastructure.kafka.KafkaOrderProducer;
import com.metao.book.shared.application.service.StageProcessor;
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
