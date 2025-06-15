package com.metao.book.order.presentation.controller;

import com.metao.book.order.application.service.OrderApplicationService;
import com.metao.book.order.domain.model.valueobject.CustomerId;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.ProductId;
import com.metao.book.order.domain.model.valueobject.Quantity;
import com.metao.book.order.presentation.dto.AddItemRequest;
import com.metao.book.order.presentation.dto.CreateOrderRequest;
import com.metao.book.order.presentation.dto.OrderResponse;
import com.metao.book.order.presentation.dto.UpdateStatusRequest;
import com.metao.book.shared.domain.financial.Money;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderApplicationService orderService;

    @PostMapping
    public ResponseEntity<OrderId> createOrder(@RequestBody CreateOrderRequest request) {
        OrderId orderId = orderService.createOrder(new CustomerId(request.getCustomerId()));
        return ResponseEntity.ok(orderId);
    }

    @PostMapping("/{orderId}/items")
    public ResponseEntity<Void> addItem(
        @PathVariable String orderId,
        @RequestBody AddItemRequest request
    ) {
        orderService.addItemToOrder(
            OrderId.of(orderId),
            new ProductId(request.getProductId()),
            request.getProductName(),
            new Quantity(request.getQuantity()),
            new Money(Currency.getInstance("USD"), BigDecimal.valueOf(request.getUnitPrice())));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Void> updateStatus(
        @PathVariable String orderId,
        @RequestBody UpdateStatusRequest request
    ) {
        orderService.updateOrderStatus(OrderId.of(orderId), request.getStatus());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getCustomerOrders(@PathVariable String customerId) {
        List<OrderResponse> orders = orderService.getCustomerOrders(new CustomerId(customerId)).stream()
            .map(OrderResponse::fromDomain)
            .toList();
        return ResponseEntity.ok(orders);
    }
}