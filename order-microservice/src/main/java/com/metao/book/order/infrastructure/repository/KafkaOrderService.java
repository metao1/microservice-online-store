package com.metao.book.order.infrastructure.repository;

import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.domain.Status;
import com.metao.book.order.infrastructure.OrderMapperInterface;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaOrderService {

    private final OrderRepository orderRepository;

    public Optional<OrderDTO> getOrder(@NotNull OrderId orderId) {
        return orderRepository.findById(orderId)
            .map(OrderMapperInterface::toOrderDTO);
    }

    public Optional<List<OrderDTO>> getOrders(@NotNull OrderId from, @NotNull OrderId to, Set<Status> statuses) {
        return Optional.of(orderRepository.findByIdBetweenAndStatusIsIn(from, to, statuses)
            .stream()
            .map(OrderMapperInterface::toOrderDTO)
            .collect(Collectors.toList()));
    }
}
