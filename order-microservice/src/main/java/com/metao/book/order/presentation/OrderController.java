package com.metao.book.order.presentation;

import com.metao.book.order.application.config.OrderEventHandler;
import com.metao.book.order.domain.OrderService;
import com.metao.book.order.domain.OrderStatus;
import com.metao.book.order.domain.dto.OrderDTO;
import com.metao.book.order.domain.exception.OrderNotFoundException;
import com.metao.book.order.domain.mapper.OrderDTOMapper;
import com.metao.book.order.infrastructure.kafka.KafkaOrderMapper;
import jakarta.validation.Valid;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@Import(OrderEventHandler.class)
@RequestMapping(path = "/order")
public class OrderController {

    private final OrderService orderService;
    private final OrderEventHandler orderEventHandler;

    @GetMapping
    public OrderDTO getOrderByOrderId(@RequestParam("order_id") String orderId) {
        return orderService.getOrderByOrderId(orderId)
            .map(OrderDTOMapper::toOrderDTO)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @GetMapping("/{pageSize}/{offset}/{sortByFieldName}")
    public Page<OrderDTO> getOrderByProductIdsAndStatusesPageable(
        @RequestParam(value = "productIds", required = false) Set<String> productIds,
        @RequestParam(value = "statuses", required = false) Set<OrderStatus> statuses,
        @PathVariable int offset,
        @PathVariable int pageSize,
        @PathVariable String sortByFieldName
    ) {
        return orderService.getOrderByProductIdsAndOrderStatus(productIds, statuses, offset, pageSize, sortByFieldName)
            .map(OrderDTOMapper::toOrderDTO);
    }

    @PostMapping
    public String createOrder(@RequestBody @Valid OrderDTO orderDto) {
        var orderCreatedEvent = KafkaOrderMapper.toOrderCreatedEvent(orderDto);
        return orderEventHandler.handle(orderCreatedEvent.getId(), orderCreatedEvent);
    }

    @PutMapping
    public String updateOrder(@RequestBody @Valid OrderDTO orderDto) {
        var updatedOrder = KafkaOrderMapper.toOrderUpdatedEvent(orderDto);
        return orderEventHandler.handle(updatedOrder.getId(), updatedOrder);
    }
}
