package com.metao.book.order.presentation;

import com.metao.book.order.application.dto.CreateOrderDTO;
import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.application.dto.exception.FindOrderException;
import com.metao.book.order.application.service.OrderService;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.domain.Status;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/order")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public OrderDTO getOrderByOrderId(@RequestParam("order_id") OrderId orderId) {
        return orderService.getOrderByOrderId(orderId).orElseThrow(FindOrderException::new);
    }

    @GetMapping("/{pageSize}/{offset}/{sortByFieldName}")
    public Page<OrderDTO> getOrderByProductIdsAndStatusesPageable(
        @RequestParam(value = "productIds", required = false) Set<String> productIds,
        @RequestParam(value = "statuses", required = false) Set<Status> statuses,
        @PathVariable int offset,
        @PathVariable int pageSize,
        @PathVariable String sortByFieldName
    ) {
        return orderService.getOrderByProductIdsAndOrderStatus(productIds, statuses, offset, pageSize, sortByFieldName);
    }

    @PostMapping
    public String createOrder(@RequestBody @Validated CreateOrderDTO orderDto) {
        return orderService.createOrder(orderDto).orElseThrow();
    }
}
