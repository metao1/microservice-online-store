package com.metao.book.order.infrastructure.repository;

import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.infrastructure.OrderMapperInterface;
import com.metao.book.shared.OrderEvent;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaOrderService {

    private static final String ORDERS = "ORDERS";
    private final OrderMapperInterface conversionService;

    public Optional<OrderDTO> getOrder(@NotNull String orderId) {
        return Optional.empty();
    }

    public List<OrderEvent> getOrders(@NotNull String from, @NotNull String to) {
        return Collections.emptyList();
    }
}
