package com.metao.book.order.presentation.controller;

import com.metao.book.order.application.service.OrderApplicationService;
import com.metao.book.order.domain.model.valueobject.CustomerId;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.ProductId;
import com.metao.book.order.domain.model.valueobject.Quantity;
import com.metao.book.order.presentation.dto.AddItemRequestDto;
import com.metao.book.order.presentation.dto.CreateOrderRequest;
import com.metao.book.order.presentation.dto.OrderResponse;
import com.metao.book.order.presentation.dto.UpdateStatusRequestDto;
import com.metao.book.shared.domain.financial.Money;
import jakarta.validation.Valid;
import java.util.Currency;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderApplicationService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderId createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(new CustomerId(request.getCustomerId()));
    }

    @PostMapping("/{orderId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public void addItem(
        @PathVariable String orderId,
        @RequestBody AddItemRequestDto request
    ) {
        orderService.addItemToOrder(
            OrderId.of(orderId),
            new ProductId(request.sku()),
            new Quantity(request.quantity()),
            new Money(Currency.getInstance("USD"), request.unitPrice()));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Void> updateStatus(
        @PathVariable String orderId,
        @RequestBody UpdateStatusRequestDto request
    ) {
        orderService.updateOrderStatus(OrderId.of(orderId), request.status());
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