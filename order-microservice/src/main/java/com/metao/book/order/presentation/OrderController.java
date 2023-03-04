package com.metao.book.order.presentation;

import com.metao.book.order.application.dto.CreateOrderDTO;
import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.domain.OrderServiceInterface;
import com.metao.book.order.domain.Status;
import com.metao.book.order.infrastructure.OrderMapperInterface;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/order")
public class OrderController {

    private final OrderServiceInterface orderService;

    @GetMapping
    public ResponseEntity<OrderDTO> getOrderByOrderId(
        @RequestParam(name = "order_id", value = "order_id") OrderId orderId
    ) {
        return orderService
            .getOrderByOrderId(orderId)
            .map(OrderMapperInterface::toOrderDTO)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<OrderDTO>> getOrderByOrderId(
        @RequestParam("product_id") Set<String> productId,
        @RequestParam("status") Set<Status> status
    ) {
        return orderService
            .getOrderByProductIdsAndOrderStatus(productId, status)
            .map(sd -> sd.stream().map(OrderMapperInterface::toOrderDTO).collect(Collectors.toList()))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public ResponseEntity<List<OrderDTO>> getAllOrders(
        @RequestParam(name = "from") OrderId from,
        @RequestParam(name = "to") OrderId to,
        @RequestParam(name = "status") Set<Status> status
    ) {
        return orderService
            .getAllOrdersPageable(from, to, status)
            .map(sd -> sd.stream().map(OrderMapperInterface::toOrderDTO).collect(Collectors.toList()))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody @Validated CreateOrderDTO orderDto) {
        return orderService.createOrder(orderDto)
            .map(ResponseEntity::ok)
            .orElseThrow();
    }
}
